package com.ridealongug.backend.services.driver;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.BookingModel;
import com.ridealongug.backend.models.database.DriverProfileModel;
import com.ridealongug.backend.models.database.VehicleCategoryModel;
import com.ridealongug.backend.models.database.VehicleModel;
import com.ridealongug.backend.models.enums.BookingStatus;
import com.ridealongug.backend.repositories.BookingRepository;
import com.ridealongug.backend.repositories.DriverProfileRepository;
import com.ridealongug.backend.repositories.VehicleCategoryRepository;
import com.ridealongug.backend.repositories.VehicleRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverProfileService extends BaseWebActionsService {

    private final DriverProfileRepository driverProfileRepository;
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;


    private static final BigDecimal DRIVER_FEE_PER_DAY = BigDecimal.valueOf(50000);

    private OperationReturnObject createProfile(JSONObject request) {
        requiresAuth();
        hasRole("DRIVER");

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("licence_number");
        requiredFields.add("licence_class");
        requires(requiredFields, request);

        Long userId = authenticatedUser().getId();

        DriverProfileModel profile = DriverProfileModel.builder()
                .userId(userId)
                .licenceNumber(request.getString("licence_number"))
                .licenceClass(request.getString("licence_class"))
                .yearsOfExperience(request.getInteger("years_of_experience"))
                .isAvailable(true)
                .build();

        DriverProfileModel saved = driverProfileRepository.save(profile);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Driver profile created", saved);
        return res;
    }

    private OperationReturnObject updateAvailability(JSONObject request) {
        requiresAuth();
        can("CAN_UPDATE_AVAILABILITY", null);
        requires("is_available", request);

        Long userId = authenticatedUser().getId();
        DriverProfileModel profile = driverProfileRepository.findFirstByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Driver profile not found"));

        boolean requestedAvailable = request.getBoolean("is_available");
        if (requestedAvailable) {
            boolean onActiveTrip = bookingRepository.findAllByDriverId(profile.getId()).stream()
                    .anyMatch(b -> b.getStatus() == BookingStatus.ONGOING || b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PICKUP_PENDING);
            if (onActiveTrip) {
                throw new IllegalStateException("You cannot mark yourself available while assigned to an active journey");
            }
        }
        profile.setIsAvailable(requestedAvailable);
        DriverProfileModel saved = driverProfileRepository.save(profile);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Availability updated", saved);
        return res;
    }



    private OperationReturnObject listAvailable() {
        requiresAuth();
        List<DriverProfileModel> drivers = driverProfileRepository.findAllByIsAvailableTrue();
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, drivers);
        return res;
    }



    private OperationReturnObject myProfile() {
        requiresAuth();
        Long userId = authenticatedUser().getId();
        DriverProfileModel profile = driverProfileRepository.findFirstByUserId(userId).orElse(null);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, profile);
        return res;
    }



    private OperationReturnObject myDashboard() {
        requiresAuth();
        Long userId = authenticatedUser().getId();
        DriverProfileModel profile = driverProfileRepository.findFirstByUserId(userId).orElse(null);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("profile", profile);

        if (profile == null) {
            dashboard.put("currentJourney", null);
            dashboard.put("trips", List.of());
            dashboard.put("carsUsed", List.of());
            dashboard.put("carsAssignedByClass", Map.of());
            dashboard.put("totalEarnings", BigDecimal.ZERO);
            dashboard.put("isAvailable", null);
            OperationReturnObject res = new OperationReturnObject();
            res.setReturnCodeAndReturnObject(0, dashboard);
            return res;
        }

        List<BookingModel> assignedTrips = bookingRepository.findAllByDriverId(profile.getId());

        BookingModel currentJourney = assignedTrips.stream()
                .filter(b -> b.getStatus() == BookingStatus.ONGOING || b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PICKUP_PENDING)
                .findFirst().orElse(null);

        List<BookingModel> completedTrips = assignedTrips.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());

        List<Long> vehicleIds = assignedTrips.stream()
                .map(BookingModel::getVehicleId).distinct().collect(Collectors.toList());
        List<VehicleModel> vehicles = vehicleIds.isEmpty() ? List.of() : vehicleRepository.findAllById(vehicleIds);

        Map<Long, String> categoryNames = new HashMap<>();
        for (VehicleModel v : vehicles) {
            if (v.getCategoryId() != null && !categoryNames.containsKey(v.getCategoryId())) {
                vehicleCategoryRepository.findById(v.getCategoryId())
                        .ifPresent(c -> categoryNames.put(v.getCategoryId(), c.getCategoryName()));
            }
        }

        Map<String, Long> carsAssignedByClass = vehicles.stream()
                .collect(Collectors.groupingBy(
                        v -> categoryNames.getOrDefault(v.getCategoryId(), "Uncategorised"),
                        Collectors.counting()));

        BigDecimal totalEarnings = BigDecimal.ZERO;
        for (BookingModel trip : completedTrips) {
            long days = Math.max(1, java.time.Duration.between(
                    trip.getStartDate().toInstant(), trip.getEndDate().toInstant()).toDays());
            totalEarnings = totalEarnings.add(DRIVER_FEE_PER_DAY.multiply(BigDecimal.valueOf(days)));
        }
        totalEarnings = totalEarnings.setScale(2, RoundingMode.HALF_UP);

        dashboard.put("currentJourney", currentJourney);
        dashboard.put("trips", assignedTrips);
        dashboard.put("carsUsed", vehicles);
        dashboard.put("carsAssignedByClass", carsAssignedByClass);
        dashboard.put("totalEarnings", totalEarnings);
        dashboard.put("isAvailable", profile.getIsAvailable());

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, dashboard);
        return res;
    }


    private OperationReturnObject listAll() {
        requiresAuth();
        can("CAN_MANAGE_DRIVERS", null);
        List<DriverProfileModel> drivers = driverProfileRepository.findAll();
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, drivers);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "createProfile" -> createProfile(request);
            case "updateAvailability" -> updateAvailability(request);
            case "listAvailable" -> listAvailable();
            case "myProfile" -> myProfile();
            case "myDashboard" -> myDashboard();
            case "listAll" -> listAll();
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

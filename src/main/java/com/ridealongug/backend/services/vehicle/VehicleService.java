package com.ridealongug.backend.services.vehicle;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.*;
import com.ridealongug.backend.models.enums.MechanicalStatus;
import com.ridealongug.backend.models.jpahelpers.sortingAndFiltering.SearchRequest;
import com.ridealongug.backend.models.jpahelpers.sortingAndFiltering.SearchSpecification;
import com.ridealongug.backend.repositories.*;
import com.ridealongug.backend.services.audit.AuditLogService;
import com.ridealongug.backend.services.notification.NotificationService;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VehicleService extends BaseWebActionsService {

    private final VehicleRepository vehicleRepository;
    private final VehicleImageRepository vehicleImageRepository;
    private final VehicleInspectionRepository vehicleInspectionRepository;
    private final VehicleInsuranceRepository vehicleInsuranceRepository;
    private final VehicleServiceRecordRepository vehicleServiceRecordRepository;
    private final VehicleAvailabilityService vehicleAvailabilityService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final com.ridealongug.backend.repositories.DealerRepository dealerRepository;
    private final com.ridealongug.backend.repositories.BookingRepository bookingRepository;
    private final com.ridealongug.backend.repositories.DriverProfileRepository driverProfileRepository;
    private final com.ridealongug.backend.repositories.SystemUserRepository systemUserRepository;



    private OperationReturnObject createVehicle(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_VEHICLES", null);

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("plate_number");
        requiredFields.add("make");
        requiredFields.add("model");
        requiredFields.add("category_id");
        requiredFields.add("ownership_type");
        requiredFields.add("fuel_consumption_per_km");
        requires(requiredFields, request);

        VehicleModel vehicle = VehicleModel.builder()
                .plateNumber(request.getString("plate_number"))
                .make(request.getString("make"))
                .model(request.getString("model"))
                .year(request.getInteger("year"))
                .categoryId(request.getLong("category_id"))
                .fuelType(request.getObject("fuel_type", com.ridealongug.backend.models.enums.FuelType.class))
                .fuelCapacityLitres(request.getDouble("fuel_capacity_litres"))
                .fuelConsumptionPerKm(request.getDouble("fuel_consumption_per_km"))
                .transmissionType(request.getObject("transmission_type", com.ridealongug.backend.models.enums.TransmissionType.class))
                .seatingCapacity(request.getInteger("seating_capacity"))
                .dealerId(request.getLong("dealer_id"))
                .ownershipType(request.getObject("ownership_type", com.ridealongug.backend.models.enums.OwnershipType.class))
                .mechanicalStatus(MechanicalStatus.PENDING_VERIFICATION)
                .isVisible(false)
                .dailyRate(request.getObject("daily_rate", BigDecimal.class))
                .location(request.getString("location"))
                .currentMileage(request.getLong("current_mileage") == null ? 0L : request.getLong("current_mileage"))
                .build();

        VehicleModel saved = vehicleRepository.save(vehicle);

        auditLogService.log(authenticatedUser().getId(), "CREATED_VEHICLE", "VehicleModel", saved.getId(),
                "Plate: " + saved.getPlateNumber());

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Vehicle created - pending inspection and insurance before it can be listed", saved);
        return res;
    }



    private OperationReturnObject submitOwnerVehicle(JSONObject request) {
        requiresAuth();
        can("CAN_SUBMIT_VEHICLE", null);

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("plate_number");
        requiredFields.add("make");
        requiredFields.add("model");
        requiredFields.add("category_id");
        requiredFields.add("fuel_consumption_per_km");
        requires(requiredFields, request);

        Long ownerId = authenticatedUser().getId();
        var dealerProfile = dealerRepository.findFirstByUserId(ownerId);
        Long dealerId = dealerProfile.map(com.ridealongug.backend.models.database.DealerModel::getId).orElse(null);
        com.ridealongug.backend.models.enums.OwnershipType ownershipType = dealerId != null
                ? com.ridealongug.backend.models.enums.OwnershipType.DEALER_SUPPLIED
                : com.ridealongug.backend.models.enums.OwnershipType.OWNER_SUPPLIED;

        VehicleModel vehicle = VehicleModel.builder()
                .plateNumber(request.getString("plate_number"))
                .make(request.getString("make"))
                .model(request.getString("model"))
                .year(request.getInteger("year"))
                .categoryId(request.getLong("category_id"))
                .fuelType(request.getObject("fuel_type", com.ridealongug.backend.models.enums.FuelType.class))
                .fuelCapacityLitres(request.getDouble("fuel_capacity_litres"))
                .fuelConsumptionPerKm(request.getDouble("fuel_consumption_per_km"))
                .transmissionType(request.getObject("transmission_type", com.ridealongug.backend.models.enums.TransmissionType.class))
                .seatingCapacity(request.getInteger("seating_capacity"))
                .ownerId(ownerId)
                .dealerId(dealerId)
                .ownershipType(ownershipType)
                .mechanicalStatus(MechanicalStatus.PENDING_VERIFICATION)
                .isVisible(false)
                .dailyRate(request.getObject("daily_rate", BigDecimal.class))
                .location(request.getString("location"))
                .currentMileage(request.getLong("current_mileage") == null ? 0L : request.getLong("current_mileage"))
                .build();

        VehicleModel saved = vehicleRepository.save(vehicle);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0,
                "Vehicle submitted - pending Super Admin inspection, insurance check, and servicing before it goes live",
                saved);
        return res;
    }

    private OperationReturnObject myVehicles() {
        requiresAuth();
        can("CAN_MANAGE_OWN_VEHICLES", null);
        Long ownerId = authenticatedUser().getId();
        List<VehicleModel> vehicles = vehicleRepository.findAll().stream()
                .filter(v -> ownerId.equals(v.getOwnerId()))
                .toList();
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, vehicles);
        return res;
    }

    private OperationReturnObject updateVehicle(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_VEHICLES", null);
        requires("id", request);

        VehicleModel vehicle = vehicleRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));

        if (request.containsKey("make")) vehicle.setMake(request.getString("make"));
        if (request.containsKey("model")) vehicle.setModel(request.getString("model"));
        if (request.containsKey("year")) vehicle.setYear(request.getInteger("year"));
        if (request.containsKey("daily_rate")) vehicle.setDailyRate(request.getObject("daily_rate", BigDecimal.class));
        if (request.containsKey("location")) vehicle.setLocation(request.getString("location"));

        VehicleModel saved = vehicleRepository.save(vehicle);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Vehicle updated successfully", saved);
        return res;
    }



    private OperationReturnObject driverForVehicle(JSONObject request) {
        requiresAuth();
        requires("vehicle_id", request);
        Long vehicleId = request.getLong("vehicle_id");

        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));

        Long callerId = authenticatedUser().getId();
        boolean isOwner = callerId.equals(vehicle.getOwnerId());
        if (!isOwner) {
            can("CAN_MANAGE_VEHICLES", null);
        }

        List<com.ridealongug.backend.models.enums.BookingStatus> activeStatuses = List.of(
                com.ridealongug.backend.models.enums.BookingStatus.PENDING,
                com.ridealongug.backend.models.enums.BookingStatus.PICKUP_PENDING,
                com.ridealongug.backend.models.enums.BookingStatus.CONFIRMED,
                com.ridealongug.backend.models.enums.BookingStatus.ONGOING);

        var activeBooking = bookingRepository.findAllByVehicleIdInAndStatusIn(List.of(vehicleId), activeStatuses)
                .stream().filter(b -> Boolean.TRUE.equals(b.getWithDriver()) && b.getDriverId() != null)
                .findFirst();

        Map<String, Object> result = new java.util.HashMap<>();
        if (activeBooking.isEmpty()) {
            result.put("driver", null);
            result.put("booking", null);
        } else {
            var booking = activeBooking.get();
            var driverProfile = driverProfileRepository.findById(booking.getDriverId()).orElse(null);
            var driverUser = driverProfile == null ? null
                    : systemUserRepository.findById(driverProfile.getUserId()).orElse(null);
            if (driverUser != null) {
                driverUser.setPassword(null);
            }
            Map<String, Object> driverInfo = new java.util.HashMap<>();
            driverInfo.put("profile", driverProfile);
            driverInfo.put("user", driverUser);
            result.put("driver", driverInfo);
            result.put("booking", booking);
        }

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, result);
        return res;
    }

    private OperationReturnObject search(JSONObject request) {
        SearchRequest searchRequest = request.getObject("SEARCH", SearchRequest.class);
        if (searchRequest == null) {
            searchRequest = new SearchRequest();
        }
        Page<VehicleModel> page = vehicleRepository.findAll(
                new SearchSpecification<>(searchRequest),
                SearchSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize())
        );
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, page);
        return res;
    }

    private OperationReturnObject browseAvailable() {
        List<VehicleModel> vehicles = vehicleRepository.findAllByIsVisibleTrue();
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, vehicles);
        return res;
    }

    private OperationReturnObject getOne(JSONObject request) {
        requires("id", request);
        VehicleModel vehicle = vehicleRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, vehicle);
        return res;
    }



    private OperationReturnObject fleetOverview() {
        requiresAuth();
        can("CAN_MANAGE_VEHICLES", null);

        List<VehicleModel> vehicles = vehicleRepository.findAll();
        List<com.ridealongug.backend.models.enums.BookingStatus> activeStatuses = List.of(
                com.ridealongug.backend.models.enums.BookingStatus.PENDING,
                com.ridealongug.backend.models.enums.BookingStatus.PICKUP_PENDING,
                com.ridealongug.backend.models.enums.BookingStatus.CONFIRMED,
                com.ridealongug.backend.models.enums.BookingStatus.ONGOING);

        List<Map<String, Object>> overview = new ArrayList<>();
        for (VehicleModel vehicle : vehicles) {
            List<BookingModel> vehicleBookings = bookingRepository.findAllByVehicleId(vehicle.getId());

            boolean onTrip = vehicleBookings.stream().anyMatch(b ->
                    b.getStatus() == com.ridealongug.backend.models.enums.BookingStatus.ONGOING
                            || activeStatuses.contains(b.getStatus()));

            List<BookingModel> completedTrips = vehicleBookings.stream()
                    .filter(b -> b.getStatus() == com.ridealongug.backend.models.enums.BookingStatus.COMPLETED)
                    .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                    .toList();

            String liveStatus;
            if (vehicle.getMechanicalStatus() == MechanicalStatus.UNDER_MAINTENANCE) {
                liveStatus = "UNDER_MAINTENANCE";
            } else if (onTrip) {
                liveStatus = "ON_TRIP";
            } else if (Boolean.TRUE.equals(vehicle.getIsVisible())) {
                liveStatus = "AVAILABLE";
            } else {
                liveStatus = "HIDDEN";
            }

            BigDecimal totalRevenue = completedTrips.stream()
                    .map(BookingModel::getTotalCost)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            double totalDistanceKm = completedTrips.stream()
                    .mapToDouble(b -> b.getEstimatedDistanceKm() == null ? 0 : b.getEstimatedDistanceKm())
                    .sum();

            Map<String, Object> row = new java.util.HashMap<>();
            row.put("vehicle", vehicle);
            row.put("liveStatus", liveStatus);
            row.put("totalTrips", completedTrips.size());
            row.put("totalDistanceKm", totalDistanceKm);
            row.put("totalRevenue", totalRevenue);
            row.put("lastDestination", completedTrips.isEmpty() ? null : completedTrips.get(0).getDropoffLocation());
            row.put("lastTripEndedAt", completedTrips.isEmpty() ? null : completedTrips.get(0).getEndDate());
            row.put("hasBookingHistory", !vehicleBookings.isEmpty());
            overview.add(row);
        }

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, overview);
        return res;
    }



    private OperationReturnObject setVisibility(JSONObject request) {
        requiresAuth();
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("vehicle_id");
        requiredFields.add("is_visible");
        requires(requiredFields, request);

        VehicleModel vehicle = vehicleRepository.findById(request.getLong("vehicle_id"))
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));

        Long callerId = authenticatedUser().getId();
        if (!callerId.equals(vehicle.getOwnerId())) {
            can("CAN_MANAGE_VEHICLES", null);
        }

        vehicle.setIsVisible(request.getBoolean("is_visible"));
        VehicleModel saved = vehicleRepository.save(vehicle);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Visibility updated", saved);
        return res;
    }



    private OperationReturnObject deleteVehicle(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_VEHICLES", null);
        requires("vehicle_id", request);

        Long vehicleId = request.getLong("vehicle_id");
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));

        boolean hasBookingHistory = !bookingRepository.findAllByVehicleId(vehicleId).isEmpty();
        if (hasBookingHistory) {
            throw new IllegalStateException(
                    "This vehicle has booking history and cannot be deleted (that would orphan those records). "
                            + "Use \"Hide from listings\" instead to take it off the platform.");
        }

        vehicleImageRepository.deleteAll(vehicleImageRepository.findAllByVehicleId(vehicleId));
        vehicleInspectionRepository.deleteAll(vehicleInspectionRepository.findAllByVehicleId(vehicleId));
        vehicleInsuranceRepository.deleteAll(vehicleInsuranceRepository.findAllByVehicleId(vehicleId));
        vehicleServiceRecordRepository.deleteAll(vehicleServiceRecordRepository.findAllByVehicleId(vehicleId));
        vehicleRepository.delete(vehicle);

        auditLogService.log(authenticatedUser().getId(), "DELETED_VEHICLE", "VehicleModel", vehicleId,
                "Deleted vehicle " + vehicle.getMake() + " " + vehicle.getModel() + " (" + vehicle.getPlateNumber() + ")");

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Vehicle deleted", null);
        return res;
    }



    private OperationReturnObject addImage(JSONObject request) {
        requiresAuth();
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("vehicle_id");
        requiredFields.add("image_base64");
        requires(requiredFields, request);

        Long vehicleId = request.getLong("vehicle_id");
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));

        boolean isOwnerOfThisVehicle = vehicle.getOwnerId() != null
                && vehicle.getOwnerId().equals(authenticatedUser().getId());
        if (!isOwnerOfThisVehicle) {
            can("CAN_MANAGE_VEHICLES", null);
        }

        VehicleImageModel image = VehicleImageModel.builder()
                .vehicleId(vehicleId)
                .imageBase64(request.getString("image_base64"))
                .isPrimary(Boolean.TRUE.equals(request.getBoolean("is_primary")))
                .build();

        VehicleImageModel saved = vehicleImageRepository.save(image);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Image added successfully", saved);
        return res;
    }



    private OperationReturnObject addImages(JSONObject request) {
        requiresAuth();
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("vehicle_id");
        requiredFields.add("images");
        requires(requiredFields, request);

        Long vehicleId = request.getLong("vehicle_id");
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));

        boolean isOwnerOfThisVehicle = vehicle.getOwnerId() != null
                && vehicle.getOwnerId().equals(authenticatedUser().getId());
        if (!isOwnerOfThisVehicle) {
            can("CAN_MANAGE_VEHICLES", null);
        }

        List<String> images = request.getJSONArray("images").toJavaList(String.class);
        if (images.size() < 2 || images.size() > 4) {
            throw new IllegalArgumentException("Please provide between 2 and 4 images");
        }

        List<VehicleImageModel> saved = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            VehicleImageModel image = VehicleImageModel.builder()
                    .vehicleId(vehicleId)
                    .imageBase64(images.get(i))
                    .isPrimary(i == 0)
                    .build();
            saved.add(vehicleImageRepository.save(image));
        }

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, saved.size() + " images added successfully", saved);
        return res;
    }

    private OperationReturnObject listImages(JSONObject request) {
        requires("vehicle_id", request);
        List<VehicleImageModel> images = vehicleImageRepository.findAllByVehicleId(request.getLong("vehicle_id"));
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, images);
        return res;
    }



    private OperationReturnObject recordInspection(JSONObject request) {
        requiresAuth();
        can("CAN_VERIFY_VEHICLE", null);
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("vehicle_id");
        requiredFields.add("mechanical_condition");
        requires(requiredFields, request);

        Long vehicleId = request.getLong("vehicle_id");
        Long adminId = authenticatedUser().getId();

        VehicleInspectionModel inspection = VehicleInspectionModel.builder()
                .vehicleId(vehicleId)
                .inspectedBySuperAdminId(adminId)
                .mechanicalCondition(request.getObject("mechanical_condition", com.ridealongug.backend.models.enums.MechanicalCondition.class))
                .notes(request.getString("notes"))
                .nextInspectionDueDate(request.getObject("next_inspection_due_date", LocalDate.class))
                .build();
        vehicleInspectionRepository.save(inspection);

        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));

        boolean approved = inspection.getMechanicalCondition() == com.ridealongug.backend.models.enums.MechanicalCondition.GOOD;
        vehicle.setMechanicalStatus(approved ? MechanicalStatus.APPROVED : MechanicalStatus.REJECTED);
        vehicle.setVerifiedBySuperAdminId(adminId);
        vehicle.setVerifiedAt(new Timestamp(System.currentTimeMillis()));
        vehicleRepository.save(vehicle);

        auditLogService.log(adminId,
                approved ? "APPROVED_VEHICLE" : "REJECTED_VEHICLE",
                "VehicleModel", vehicleId, request.getString("notes"));

        vehicleAvailabilityService.refreshVisibility(vehicleId);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Inspection recorded", inspection);
        return res;
    }

    private OperationReturnObject inspectionHistory(JSONObject request) {
        requires("vehicle_id", request);
        List<VehicleInspectionModel> history =
                vehicleInspectionRepository.findAllByVehicleIdOrderByInspectionDateDesc(request.getLong("vehicle_id"));
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, history);
        return res;
    }



    private OperationReturnObject markUnderMaintenance(JSONObject request) {
        requiresAuth();
        can("CAN_VERIFY_VEHICLE", null);
        requires("vehicle_id", request);

        Long vehicleId = request.getLong("vehicle_id");
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));
        vehicle.setMechanicalStatus(MechanicalStatus.UNDER_MAINTENANCE);
        VehicleModel saved = vehicleRepository.save(vehicle);

        auditLogService.log(authenticatedUser().getId(), "MARKED_UNDER_MAINTENANCE", "VehicleModel", vehicleId,
                request.getString("notes"));
        vehicleAvailabilityService.refreshVisibility(vehicleId);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Vehicle marked under maintenance", saved);
        return res;
    }



    private OperationReturnObject markRepaired(JSONObject request) {
        requiresAuth();
        can("CAN_VERIFY_VEHICLE", null);
        requires("vehicle_id", request);

        Long vehicleId = request.getLong("vehicle_id");
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));
        vehicle.setMechanicalStatus(MechanicalStatus.PENDING_VERIFICATION);
        VehicleModel saved = vehicleRepository.save(vehicle);

        auditLogService.log(authenticatedUser().getId(), "MARKED_REPAIRED_PENDING_REINSPECTION", "VehicleModel", vehicleId,
                request.getString("notes"));
        vehicleAvailabilityService.refreshVisibility(vehicleId);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Vehicle sent back for re-inspection", saved);
        return res;
    }



    private OperationReturnObject addInsurance(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_VEHICLES", null);
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("vehicle_id");
        requiredFields.add("insurance_provider");
        requiredFields.add("policy_number");
        requiredFields.add("insurance_type");
        requiredFields.add("coverage_start_date");
        requiredFields.add("coverage_expiry_date");
        requires(requiredFields, request);

        Long vehicleId = request.getLong("vehicle_id");

        VehicleInsuranceModel insurance = VehicleInsuranceModel.builder()
                .vehicleId(vehicleId)
                .insuranceProvider(request.getString("insurance_provider"))
                .policyNumber(request.getString("policy_number"))
                .insuranceType(request.getObject("insurance_type", com.ridealongug.backend.models.enums.InsuranceType.class))
                .coverageStartDate(request.getObject("coverage_start_date", LocalDate.class))
                .coverageExpiryDate(request.getObject("coverage_expiry_date", LocalDate.class))
                .documentBase64(request.getString("document_base64"))
                .build();

        VehicleInsuranceModel saved = vehicleInsuranceRepository.save(insurance);

        vehicleAvailabilityService.refreshVisibility(vehicleId);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Insurance policy recorded", saved);
        return res;
    }

    private OperationReturnObject insuranceHistory(JSONObject request) {
        requires("vehicle_id", request);
        List<VehicleInsuranceModel> policies = vehicleInsuranceRepository.findAllByVehicleId(request.getLong("vehicle_id"));
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, policies);
        return res;
    }



    private OperationReturnObject recordService(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_VEHICLES", null);
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("vehicle_id");
        requiredFields.add("serviced_at_mileage");
        requiredFields.add("service_date");
        requiredFields.add("next_service_due_mileage");
        requires(requiredFields, request);

        Long vehicleId = request.getLong("vehicle_id");

        VehicleServiceRecordModel record = VehicleServiceRecordModel.builder()
                .vehicleId(vehicleId)
                .servicedAtMileage(request.getLong("serviced_at_mileage"))
                .serviceDate(request.getObject("service_date", LocalDate.class))
                .serviceType(request.getString("service_type"))
                .servicedBy(request.getString("serviced_by"))
                .notes(request.getString("notes"))
                .nextServiceDueMileage(request.getLong("next_service_due_mileage"))
                .build();
        vehicleServiceRecordRepository.save(record);

        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));
        vehicle.setCurrentMileage(record.getServicedAtMileage());
        vehicle.setNextServiceDueMileage(record.getNextServiceDueMileage());
        vehicle.setLastServiceDate(record.getServiceDate());
        vehicleRepository.save(vehicle);

        vehicleAvailabilityService.refreshVisibility(vehicleId);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Service record added, vehicle mileage updated", record);
        return res;
    }

    private OperationReturnObject serviceHistory(JSONObject request) {
        requires("vehicle_id", request);
        List<VehicleServiceRecordModel> history =
                vehicleServiceRecordRepository.findAllByVehicleIdOrderByServiceDateDesc(request.getLong("vehicle_id"));
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, history);
        return res;
    }



    public void incrementMileageAfterTrip(Long vehicleId, Double distanceKm) {
        if (distanceKm == null) return;
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));
        long current = vehicle.getCurrentMileage() == null ? 0L : vehicle.getCurrentMileage();
        vehicle.setCurrentMileage(current + Math.round(distanceKm));
        vehicleRepository.save(vehicle);
        vehicleAvailabilityService.refreshVisibility(vehicleId);
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "createVehicle" -> createVehicle(request);
            case "submitOwnerVehicle" -> submitOwnerVehicle(request);
            case "myVehicles" -> myVehicles();
            case "driverForVehicle" -> driverForVehicle(request);
            case "updateVehicle" -> updateVehicle(request);
            case "search" -> search(request);
            case "browseAvailable" -> browseAvailable();
            case "getOne" -> getOne(request);
            case "fleetOverview" -> fleetOverview();
            case "setVisibility" -> setVisibility(request);
            case "deleteVehicle" -> deleteVehicle(request);
            case "addImage" -> addImage(request);
            case "addImages" -> addImages(request);
            case "listImages" -> listImages(request);
            case "recordInspection" -> recordInspection(request);
            case "inspectionHistory" -> inspectionHistory(request);
            case "markUnderMaintenance" -> markUnderMaintenance(request);
            case "markRepaired" -> markRepaired(request);
            case "addInsurance" -> addInsurance(request);
            case "insuranceHistory" -> insuranceHistory(request);
            case "recordService" -> recordService(request);
            case "serviceHistory" -> serviceHistory(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

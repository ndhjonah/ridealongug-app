package com.ridealongug.backend.services.booking;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.BookingModel;
import com.ridealongug.backend.models.database.DriverProfileModel;
import com.ridealongug.backend.models.database.DrivingLicenceModel;
import com.ridealongug.backend.models.database.VehicleModel;
import com.ridealongug.backend.models.enums.BookingStatus;
import com.ridealongug.backend.models.enums.NotificationType;
import com.ridealongug.backend.models.enums.VerificationStatus;
import com.ridealongug.backend.models.jpahelpers.sortingAndFiltering.SearchRequest;
import com.ridealongug.backend.models.jpahelpers.sortingAndFiltering.SearchSpecification;
import com.ridealongug.backend.repositories.*;
import com.ridealongug.backend.services.coupon.DiscountCouponService;
import com.ridealongug.backend.services.notification.NotificationService;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.services.vehicle.VehicleAvailabilityService;
import com.ridealongug.backend.services.vehicle.VehicleService;
import com.ridealongug.backend.utils.GeoUtils;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService extends BaseWebActionsService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final DrivingLicenceRepository drivingLicenceRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final VehicleAvailabilityService vehicleAvailabilityService;
    private final PricingService pricingService;
    private final VehicleService vehicleService;
    private final DiscountCouponService discountCouponService;
    private final NotificationService notificationService;
    private final com.ridealongug.backend.services.earning.EarningService earningService;
    private final PaymentRepository paymentRepository;

    private OperationReturnObject createBooking(JSONObject request) {
        requiresAuth();
        can("CAN_BOOK_VEHICLE", null);

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("vehicle_id");
        requiredFields.add("with_driver");
        requiredFields.add("pickup_location");
        requiredFields.add("dropoff_location");
        requiredFields.add("pickup_lat");
        requiredFields.add("pickup_lng");
        requiredFields.add("dropoff_lat");
        requiredFields.add("dropoff_lng");
        requiredFields.add("start_date");
        requiredFields.add("end_date");
        requires(requiredFields, request);

        Long vehicleId = request.getLong("vehicle_id");
        boolean withDriver = request.getBoolean("with_driver");
        Long customerId = authenticatedUser().getId();


        if (!vehicleAvailabilityService.isEligibleForHire(vehicleId)) {
            throw new IllegalStateException("This vehicle is not currently available for hire");
        }


        if (!withDriver) {
            DrivingLicenceModel licence = drivingLicenceRepository.findFirstByUserId(customerId)
                    .orElseThrow(() -> new IllegalStateException("Please submit your driving licence before booking without a driver"));
            if (licence.getVerificationStatus() != VerificationStatus.VERIFIED) {
                throw new IllegalStateException("Your driving licence is not yet verified");
            }
        }


        Timestamp startDate = request.getObject("start_date", Timestamp.class);
        Timestamp endDate = request.getObject("end_date", Timestamp.class);
        List<BookingModel> overlapping = bookingRepository.findAllByVehicleIdAndEndDateAfterAndStartDateBefore(
                vehicleId, startDate, endDate);
        overlapping.removeIf(b -> b.getStatus() == BookingStatus.CANCELLED || b.getStatus() == BookingStatus.COMPLETED);
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Vehicle is already booked for the requested dates");
        }




        Long driverId = null;
        if (withDriver) {
            requires("driver_id", request);
            Long requestedDriverId = request.getLong("driver_id");
            DriverProfileModel driver = driverProfileRepository.findById(requestedDriverId)
                    .orElseThrow(() -> new IllegalStateException("Selected driver not found"));
            if (!Boolean.TRUE.equals(driver.getIsAvailable())) {
                throw new IllegalStateException("That driver is no longer available - please pick another");
            }
            driverId = driver.getId();
            driver.setIsAvailable(false);
            driverProfileRepository.save(driver);
        }



        double pickupLat = request.getDouble("pickup_lat");
        double pickupLng = request.getDouble("pickup_lng");
        double dropoffLat = request.getDouble("dropoff_lat");
        double dropoffLng = request.getDouble("dropoff_lng");
        double distanceKm = GeoUtils.haversineKm(pickupLat, pickupLng, dropoffLat, dropoffLng);
        double durationHours = Duration.between(startDate.toInstant(), endDate.toInstant()).toHours();
        int numberOfDays = (int) Math.max(1, Duration.between(startDate.toInstant(), endDate.toInstant()).toDays());
        BigDecimal totalCost = pricingService.calculateCost(vehicleId, distanceKm, durationHours, withDriver, numberOfDays);


        String couponCode = request.getString("coupon_code");
        if (couponCode != null && !couponCode.isBlank()) {
            double discountPercentage = discountCouponService.validateAndConsume(couponCode);
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(discountPercentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            totalCost = totalCost.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }

        BookingModel booking = BookingModel.builder()
                .customerId(customerId)
                .vehicleId(vehicleId)
                .withDriver(withDriver)
                .driverId(driverId)
                .pickupLocation(request.getString("pickup_location"))
                .dropoffLocation(request.getString("dropoff_location"))
                .pickupLat(pickupLat)
                .pickupLng(pickupLng)
                .dropoffLat(dropoffLat)
                .dropoffLng(dropoffLng)
                .estimatedDistanceKm(distanceKm)
                .startDate(startDate)
                .endDate(endDate)
                .status(BookingStatus.PENDING)
                .totalCost(totalCost)
                .build();

        BookingModel saved = bookingRepository.save(booking);

        if (driverId != null) {
            Long finalDriverId = driverId;
            driverProfileRepository.findById(finalDriverId).ifPresent(driver ->
                    notificationService.notify(driver.getUserId(), "New Journey Assigned",
                            "You have been booked for a journey from " + saved.getPickupLocation() + " to "
                                    + saved.getDropoffLocation() + ". Please prepare accordingly.",
                            NotificationType.BOOKING_CONFIRMED)
            );
        }

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Booking created, awaiting confirmation", saved);
        return res;
    }



    private OperationReturnObject reassignDriver(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_DRIVERS", null);

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("booking_id");
        requiredFields.add("driver_id");
        requires(requiredFields, request);

        BookingModel booking = bookingRepository.findById(request.getLong("booking_id"))
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (!Boolean.TRUE.equals(booking.getWithDriver())) {
            throw new IllegalStateException("This booking did not request a platform driver");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("This booking cannot be reassigned a driver from its current status: " + booking.getStatus());
        }

        DriverProfileModel newDriver = driverProfileRepository.findById(request.getLong("driver_id"))
                .orElseThrow(() -> new IllegalStateException("Driver not found"));
        if (!Boolean.TRUE.equals(newDriver.getIsAvailable())) {
            throw new IllegalStateException("That driver is not currently available");
        }


        if (booking.getDriverId() != null && !booking.getDriverId().equals(newDriver.getId())) {
            driverProfileRepository.findById(booking.getDriverId()).ifPresent(previous -> {
                previous.setIsAvailable(true);
                driverProfileRepository.save(previous);
                notificationService.notify(previous.getUserId(), "Journey Reassigned",
                        "You have been unassigned from booking #" + booking.getId() + ".",
                        NotificationType.BOOKING_CONFIRMED);
            });
        }

        booking.setDriverId(newDriver.getId());
        BookingModel saved = bookingRepository.save(booking);

        newDriver.setIsAvailable(false);
        driverProfileRepository.save(newDriver);

        notificationService.notify(newDriver.getUserId(), "New Journey Assigned",
                "You have been assigned to booking #" + booking.getId() + " (" + booking.getPickupLocation()
                        + " to " + booking.getDropoffLocation() + "). Please prepare accordingly.",
                NotificationType.BOOKING_CONFIRMED);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Driver reassigned and notified", saved);
        return res;
    }



    private OperationReturnObject confirmBooking(JSONObject request) {
        requiresAuth();
        can("CAN_VIEW_ALL_BOOKINGS", null);
        requires("id", request);

        BookingModel booking = bookingRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.PICKUP_PENDING) {
            throw new IllegalStateException("Only a pending booking can be confirmed (current status: " + booking.getStatus() + ")");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        BookingModel saved = bookingRepository.save(booking);

        notificationService.notify(booking.getCustomerId(), "Booking Confirmed",
                "Your booking #" + booking.getId() + " has been confirmed. You may start your trip whenever you're ready.",
                NotificationType.BOOKING_CONFIRMED);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Booking confirmed", saved);
        return res;
    }



    private OperationReturnObject confirmPickup(JSONObject request) {
        requiresAuth();
        requires("id", request);

        BookingModel booking = bookingRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PICKUP_PENDING) {
            throw new IllegalStateException("This booking is not awaiting pickup confirmation (current status: " + booking.getStatus() + ")");
        }

        VehicleModel vehicle = vehicleRepository.findById(booking.getVehicleId())
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));

        Long callerId = authenticatedUser().getId();
        boolean isOwner = callerId.equals(vehicle.getOwnerId());
        if (!isOwner) {
            can("CAN_MANAGE_VEHICLES", null);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        BookingModel saved = bookingRepository.save(booking);

        notificationService.notify(booking.getCustomerId(), "Pickup Confirmed",
                "The vehicle owner has confirmed your pickup for booking #" + booking.getId()
                        + ". You can now start your trip - remember to start it before "
                        + booking.getStartDate() + " and return the vehicle by " + booking.getEndDate() + ".",
                NotificationType.BOOKING_CONFIRMED);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Pickup confirmed - customer notified", saved);
        return res;
    }



    private OperationReturnObject pendingPickupsForOwner() {
        requiresAuth();
        Long ownerId = authenticatedUser().getId();
        List<Long> ownedVehicleIds = vehicleRepository.findAll().stream()
                .filter(v -> ownerId.equals(v.getOwnerId()))
                .map(VehicleModel::getId)
                .toList();

        List<BookingModel> pending = ownedVehicleIds.isEmpty() ? List.of()
                : bookingRepository.findAllByVehicleIdInAndStatusIn(ownedVehicleIds, List.of(BookingStatus.PICKUP_PENDING));

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, pending);
        return res;
    }

    private OperationReturnObject startTrip(JSONObject request) {
        requiresAuth();
        requires("id", request);

        BookingModel booking = bookingRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (!booking.getCustomerId().equals(authenticatedUser().getId())) {
            throw new IllegalStateException("You can only start your own trip");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only a confirmed booking can be started (current status: " + booking.getStatus() + ")");
        }
        if (new Timestamp(System.currentTimeMillis()).after(booking.getEndDate())) {
            throw new IllegalStateException("This booking's window has already passed - please cancel it and make a new booking");
        }

        booking.setStatus(BookingStatus.ONGOING);
        BookingModel saved = bookingRepository.save(booking);


        if (Boolean.TRUE.equals(booking.getWithDriver()) && booking.getDriverId() != null) {
            driverProfileRepository.findById(booking.getDriverId()).ifPresent(driver -> {
                if (Boolean.TRUE.equals(driver.getIsAvailable())) {
                    driver.setIsAvailable(false);
                    driverProfileRepository.save(driver);
                }
            });
        }

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Trip started", saved);
        return res;
    }

    private OperationReturnObject completeBooking(JSONObject request) {
        requiresAuth();
        requires("id", request);

        BookingModel booking = bookingRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        boolean isOwnBooking = booking.getCustomerId().equals(authenticatedUser().getId());
        if (!isOwnBooking) {
            can("CAN_VIEW_ALL_BOOKINGS", null);
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("This booking has already been completed");
        }
        if (booking.getStatus() != BookingStatus.ONGOING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("This booking cannot be completed from its current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.COMPLETED);
        BookingModel saved = bookingRepository.save(booking);

        boolean returnedLate = new Timestamp(System.currentTimeMillis()).after(booking.getEndDate());
        if (returnedLate) {
            notificationService.notify(booking.getCustomerId(), "Vehicle Returned Late",
                    "Booking #" + booking.getId() + " was completed after its scheduled end date. Late fees may apply on future bookings.",
                    NotificationType.BOOKING_CONFIRMED);
        }


        if (Boolean.TRUE.equals(booking.getWithDriver()) && booking.getDriverId() != null) {
            driverProfileRepository.findById(booking.getDriverId()).ifPresent(driver -> {
                driver.setIsAvailable(true);
                driverProfileRepository.save(driver);
            });
        }


        vehicleService.incrementMileageAfterTrip(booking.getVehicleId(), booking.getEstimatedDistanceKm());



        vehicleRepository.findById(booking.getVehicleId()).ifPresent(vehicle ->
                earningService.recordEarning(booking.getId(), vehicle.getId(), vehicle.getOwnerId(), booking.getTotalCost())
        );

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Booking completed", saved);
        return res;
    }

    private OperationReturnObject cancelBooking(JSONObject request) {
        requiresAuth();
        requires("id", request);

        BookingModel booking = bookingRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("This booking cannot be cancelled from its current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        BookingModel saved = bookingRepository.save(booking);

        if (Boolean.TRUE.equals(booking.getWithDriver()) && booking.getDriverId() != null) {
            driverProfileRepository.findById(booking.getDriverId()).ifPresent(driver -> {
                driver.setIsAvailable(true);
                driverProfileRepository.save(driver);
            });
        }

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Booking cancelled", saved);
        return res;
    }

    private OperationReturnObject myBookings() {
        requiresAuth();
        Long customerId = authenticatedUser().getId();
        List<BookingModel> bookings = bookingRepository.findAllByCustomerId(customerId);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, bookings);
        return res;
    }

    private OperationReturnObject search(JSONObject request) {
        requiresAuth();
        can("CAN_VIEW_ALL_BOOKINGS", null);

        SearchRequest searchRequest = request.getObject("SEARCH", SearchRequest.class);
        if (searchRequest == null) {
            searchRequest = new SearchRequest();
        }
        Page<BookingModel> page = bookingRepository.findAll(
                new SearchSpecification<>(searchRequest),
                SearchSpecification.getPageable(searchRequest.getPage(), searchRequest.getSize())
        );
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, page);
        return res;
    }



    private OperationReturnObject previewCost(JSONObject request) {
        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("vehicle_id");
        requiredFields.add("with_driver");
        requiredFields.add("pickup_lat");
        requiredFields.add("pickup_lng");
        requiredFields.add("dropoff_lat");
        requiredFields.add("dropoff_lng");
        requiredFields.add("start_date");
        requiredFields.add("end_date");
        requires(requiredFields, request);

        Long vehicleId = request.getLong("vehicle_id");
        boolean withDriver = request.getBoolean("with_driver");

        if (!vehicleAvailabilityService.isEligibleForHire(vehicleId)) {
            throw new IllegalStateException("This vehicle is not currently available for hire");
        }

        Timestamp startDate = request.getObject("start_date", Timestamp.class);
        Timestamp endDate = request.getObject("end_date", Timestamp.class);
        double distanceKm = GeoUtils.haversineKm(
                request.getDouble("pickup_lat"), request.getDouble("pickup_lng"),
                request.getDouble("dropoff_lat"), request.getDouble("dropoff_lng"));
        double durationHours = Duration.between(startDate.toInstant(), endDate.toInstant()).toHours();
        int numberOfDays = (int) Math.max(1, Duration.between(startDate.toInstant(), endDate.toInstant()).toDays());

        BigDecimal costBeforeDiscount = pricingService.calculateCost(vehicleId, distanceKm, durationHours, withDriver, numberOfDays);
        BigDecimal finalCost = costBeforeDiscount;

        Double discountPercentage = null;
        String couponCode = request.getString("coupon_code");
        if (couponCode != null && !couponCode.isBlank()) {
            discountPercentage = discountCouponService.peekDiscount(couponCode);
            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(discountPercentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            finalCost = costBeforeDiscount.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }

        java.util.Map<String, Object> preview = new java.util.HashMap<>();
        preview.put("estimated_distance_km", distanceKm);
        preview.put("estimated_duration_hours", durationHours);
        preview.put("number_of_days", numberOfDays);
        preview.put("with_driver", withDriver);
        preview.put("cost_before_discount", costBeforeDiscount);
        preview.put("discount_percentage", discountPercentage);
        preview.put("final_cost", finalCost);

        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, preview);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "createBooking" -> createBooking(request);
            case "confirmBooking" -> confirmBooking(request);
            case "confirmPickup" -> confirmPickup(request);
            case "pendingPickupsForOwner" -> pendingPickupsForOwner();
            case "reassignDriver" -> reassignDriver(request);
            case "startTrip" -> startTrip(request);
            case "completeBooking" -> completeBooking(request);
            case "cancelBooking" -> cancelBooking(request);
            case "myBookings" -> myBookings();
            case "search" -> search(request);
            case "previewCost" -> previewCost(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

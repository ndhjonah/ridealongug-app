package com.ridealongug.backend.services;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.services.audit.AuditLogService;
import com.ridealongug.backend.services.auth.AuthService;
import com.ridealongug.backend.services.booking.BookingService;
import com.ridealongug.backend.services.coupon.DiscountCouponService;
import com.ridealongug.backend.services.dealer.DealerService;
import com.ridealongug.backend.services.driver.DriverProfileService;
import com.ridealongug.backend.services.earning.EarningService;
import com.ridealongug.backend.services.licence.DrivingLicenceService;
import com.ridealongug.backend.services.notification.NotificationService;
import com.ridealongug.backend.services.payment.PaymentService;
import com.ridealongug.backend.services.review.ReviewService;
import com.ridealongug.backend.services.user.SystemUserModelService;
import com.ridealongug.backend.services.vehicle.VehicleCategoryService;
import com.ridealongug.backend.services.vehicle.VehicleService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebActionsService {

    private final AuthService authService;
    private final SystemUserModelService systemUserModelService;
    private final DealerService dealerService;
    private final VehicleService vehicleService;
    private final VehicleCategoryService vehicleCategoryService;
    private final BookingService bookingService;
    private final DrivingLicenceService drivingLicenceService;
    private final DriverProfileService driverProfileService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;
    private final NotificationService notificationService;
    private final DiscountCouponService discountCouponService;
    private final AuditLogService auditLogService;
    private final EarningService earningService;

    public OperationReturnObject processAction(String service, String action, JSONObject payload) {
        return switch (service) {
            case "Auth" -> authService.process(action, payload);
            case "SystemUserModelService" -> systemUserModelService.process(action, payload);
            case "DealerService" -> dealerService.process(action, payload);
            case "VehicleService" -> vehicleService.process(action, payload);
            case "VehicleCategoryService" -> vehicleCategoryService.process(action, payload);
            case "BookingService" -> bookingService.process(action, payload);
            case "DrivingLicenceService" -> drivingLicenceService.process(action, payload);
            case "DriverProfileService" -> driverProfileService.process(action, payload);
            case "PaymentService" -> paymentService.process(action, payload);
            case "ReviewService" -> reviewService.process(action, payload);
            case "NotificationService" -> notificationService.process(action, payload);
            case "DiscountCouponService" -> discountCouponService.process(action, payload);
            case "AuditLogService" -> auditLogService.process(action, payload);
            case "EarningService" -> earningService.process(action, payload);
            default -> {
                OperationReturnObject res = new OperationReturnObject();
                res.setReturnCodeAndReturnMessage(404, "UNKNOWN SERVICE");
                yield res;
            }
        };
    }
}

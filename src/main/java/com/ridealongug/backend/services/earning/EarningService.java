package com.ridealongug.backend.services.earning;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.VehicleEarningModel;
import com.ridealongug.backend.models.enums.NotificationType;
import com.ridealongug.backend.models.enums.PayoutStatus;
import com.ridealongug.backend.repositories.VehicleEarningRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.services.notification.NotificationService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EarningService extends BaseWebActionsService {

    @Value("${PLATFORM_COMMISSION_PERCENTAGE:20}")
    private Double platformCommissionPercentage;

    private final VehicleEarningRepository vehicleEarningRepository;
    private final NotificationService notificationService;



    public void recordEarning(Long bookingId, Long vehicleId, Long ownerId, BigDecimal grossAmount) {
        if (ownerId == null) {
            return;
        }
        if (vehicleEarningRepository.findFirstByBookingId(bookingId).isPresent()) {
            return;
        }
        BigDecimal commissionMultiplier = BigDecimal.valueOf(platformCommissionPercentage)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal platformAmount = grossAmount.multiply(commissionMultiplier).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ownerAmount = grossAmount.subtract(platformAmount).setScale(2, RoundingMode.HALF_UP);

        VehicleEarningModel earning = VehicleEarningModel.builder()
                .bookingId(bookingId)
                .vehicleId(vehicleId)
                .ownerId(ownerId)
                .grossAmount(grossAmount)
                .commissionPercentage(platformCommissionPercentage)
                .ownerAmount(ownerAmount)
                .platformAmount(platformAmount)
                .payoutStatus(PayoutStatus.PENDING)
                .build();
        vehicleEarningRepository.save(earning);

        notificationService.notify(ownerId, "You Earned a Payout",
                "Your vehicle earned " + ownerAmount + " from a completed booking. Payout is pending.",
                NotificationType.GENERAL);
    }

    private OperationReturnObject myEarnings() {
        requiresAuth();
        can("CAN_VIEW_OWN_EARNINGS", null);
        Long ownerId = authenticatedUser().getId();
        List<VehicleEarningModel> earnings = vehicleEarningRepository.findAllByOwnerId(ownerId);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, earnings);
        return res;
    }

    private OperationReturnObject pendingPayouts() {
        requiresAuth();
        can("CAN_MANAGE_PAYOUTS", null);
        List<VehicleEarningModel> pending = vehicleEarningRepository.findAllByPayoutStatus(PayoutStatus.PENDING);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, pending);
        return res;
    }

    private OperationReturnObject markAsPaid(JSONObject request) {
        requiresAuth();
        can("CAN_MANAGE_PAYOUTS", null);
        requires("id", request);

        VehicleEarningModel earning = vehicleEarningRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Earning record not found"));

        earning.setPayoutStatus(PayoutStatus.PAID);
        earning.setPaidAt(new Timestamp(System.currentTimeMillis()));
        VehicleEarningModel saved = vehicleEarningRepository.save(earning);

        notificationService.notify(earning.getOwnerId(), "Payout Sent",
                "Your payout of " + earning.getOwnerAmount() + " has been marked as paid.",
                NotificationType.GENERAL);

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Payout marked as paid", saved);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "myEarnings" -> myEarnings();
            case "pendingPayouts" -> pendingPayouts();
            case "markAsPaid" -> markAsPaid(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

package com.ridealongug.backend.services.licence;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.DrivingLicenceModel;
import com.ridealongug.backend.models.enums.NotificationType;
import com.ridealongug.backend.models.enums.VerificationStatus;
import com.ridealongug.backend.repositories.DrivingLicenceRepository;
import com.ridealongug.backend.services.notification.NotificationService;
import com.ridealongug.backend.services.audit.AuditLogService;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DrivingLicenceService extends BaseWebActionsService {

    private final DrivingLicenceRepository drivingLicenceRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    private OperationReturnObject submitLicence(JSONObject request) {
        requiresAuth();
        can("CAN_SUBMIT_LICENCE", null);

        List<String> requiredFields = new ArrayList<>();
        requiredFields.add("licence_number");
        requiredFields.add("licence_class");
        requiredFields.add("issue_date");
        requiredFields.add("expiry_date");
        requiredFields.add("licence_image_base64");
        requires(requiredFields, request);

        Long userId = authenticatedUser().getId();

        DrivingLicenceModel licence = DrivingLicenceModel.builder()
                .userId(userId)
                .licenceNumber(request.getString("licence_number"))
                .licenceClass(request.getString("licence_class"))
                .issueDate(request.getObject("issue_date", LocalDate.class))
                .expiryDate(request.getObject("expiry_date", LocalDate.class))
                .licenceImageBase64(request.getString("licence_image_base64"))
                .verificationStatus(VerificationStatus.PENDING)
                .build();

        DrivingLicenceModel saved = drivingLicenceRepository.save(licence);
        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Licence submitted, pending verification", saved);
        return res;
    }

    private OperationReturnObject verifyLicence(JSONObject request) {
        requiresAuth();
        can("CAN_VERIFY_LICENCE", null);
        requires("id", request);
        requires("verification_status", request);

        DrivingLicenceModel licence = drivingLicenceRepository.findById(request.getLong("id"))
                .orElseThrow(() -> new IllegalStateException("Licence not found"));

        VerificationStatus status = request.getObject("verification_status", VerificationStatus.class);
        Long adminId = authenticatedUser().getId();

        licence.setVerificationStatus(status);
        licence.setVerifiedBySuperAdminId(adminId);
        licence.setVerifiedAt(new Timestamp(System.currentTimeMillis()));
        if (status == VerificationStatus.REJECTED) {
            licence.setRejectionReason(request.getString("rejection_reason"));
        }
        DrivingLicenceModel saved = drivingLicenceRepository.save(licence);

        auditLogService.log(adminId,
                status == VerificationStatus.VERIFIED ? "VERIFIED_LICENCE" : "REJECTED_LICENCE",
                "DrivingLicenceModel", licence.getId(), licence.getRejectionReason());

        if (status == VerificationStatus.VERIFIED) {
            notificationService.notify(licence.getUserId(), "Licence Verified",
                    "Your driving licence has been verified. You can now self-drive rentals.",
                    NotificationType.LICENCE_VERIFIED);
        }

        OperationReturnObject res = new OperationReturnObject();
        res.setCodeAndMessageAndReturnObject(0, "Licence verification updated", saved);
        return res;
    }

    private OperationReturnObject myLicence() {
        requiresAuth();
        Long userId = authenticatedUser().getId();
        DrivingLicenceModel licence = drivingLicenceRepository.findFirstByUserId(userId)
                .orElse(null);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, licence);
        return res;
    }

    private OperationReturnObject pendingVerification() {
        requiresAuth();
        can("CAN_VERIFY_LICENCE", null);
        List<DrivingLicenceModel> all = drivingLicenceRepository.findAll();
        all.removeIf(l -> l.getVerificationStatus() != VerificationStatus.PENDING);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, all);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "submitLicence" -> submitLicence(request);
            case "verifyLicence" -> verifyLicence(request);
            case "myLicence" -> myLicence();
            case "pendingVerification" -> pendingVerification();
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

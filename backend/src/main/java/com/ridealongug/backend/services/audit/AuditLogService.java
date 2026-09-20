package com.ridealongug.backend.services.audit;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.AuditLogModel;
import com.ridealongug.backend.repositories.AuditLogRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService extends BaseWebActionsService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long performedByUserId, String action, String targetEntity, Long targetId, String details) {
        AuditLogModel entry = AuditLogModel.builder()
                .performedByUserId(performedByUserId)
                .action(action)
                .targetEntity(targetEntity)
                .targetId(targetId)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    }

    private OperationReturnObject viewLogs() {
        requiresAuth();
        can("CAN_VIEW_AUDIT_LOGS", null);
        List<AuditLogModel> logs = auditLogRepository.findAllByOrderByPerformedAtDesc();
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, logs);
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "viewLogs" -> viewLogs();
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

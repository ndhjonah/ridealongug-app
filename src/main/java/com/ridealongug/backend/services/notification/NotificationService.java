package com.ridealongug.backend.services.notification;

import com.alibaba.fastjson2.JSONObject;
import com.ridealongug.backend.models.database.NotificationModel;
import com.ridealongug.backend.models.enums.NotificationType;
import com.ridealongug.backend.repositories.NotificationRepository;
import com.ridealongug.backend.services.base.BaseWebActionsService;
import com.ridealongug.backend.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService extends BaseWebActionsService {

    private final NotificationRepository notificationRepository;



    public void notify(Long userId, String title, String message, NotificationType type) {
        NotificationModel notification = NotificationModel.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    private OperationReturnObject myNotifications() {
        requiresAuth();
        Long userId = authenticatedUser().getId();
        List<NotificationModel> notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnObject(0, notifications);
        return res;
    }

    private OperationReturnObject markAsRead(JSONObject request) {
        requiresAuth();
        requires("notification_id", request);
        Long notificationId = request.getLong("notification_id");
        NotificationModel notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalStateException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
        OperationReturnObject res = new OperationReturnObject();
        res.setReturnCodeAndReturnMessage(0, "Marked as read");
        return res;
    }

    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action) {
            case "myNotifications" -> myNotifications();
            case "markAsRead" -> markAsRead(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }
}

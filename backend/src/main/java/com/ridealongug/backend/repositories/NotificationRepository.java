package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.NotificationModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JetRepository<NotificationModel, Long> {
    List<NotificationModel> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<NotificationModel> findAllByUserIdAndIsReadFalse(Long userId);
}

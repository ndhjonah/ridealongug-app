package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.AuditLogModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JetRepository<AuditLogModel, Long> {
    List<AuditLogModel> findAllByPerformedByUserIdOrderByPerformedAtDesc(Long performedByUserId);
    List<AuditLogModel> findAllByOrderByPerformedAtDesc();
}

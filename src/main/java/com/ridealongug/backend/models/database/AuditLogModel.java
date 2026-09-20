package com.ridealongug.backend.models.database;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "audit_log", schema = "public")
public class AuditLogModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "performed_by_user_id")
    private Long performedByUserId;

    @Basic
    @Column(name = "action")
    private String action;

    @Basic
    @Column(name = "target_entity")
    private String targetEntity;

    @Basic
    @Column(name = "target_id")
    private Long targetId;

    @Basic
    @Column(name = "details")
    private String details;

    @Basic
    @Column(name = "performed_at")
    @CreationTimestamp
    private Timestamp performedAt;
}

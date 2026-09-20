package com.ridealongug.backend.models.database;

import com.ridealongug.backend.models.enums.NotificationType;
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
@Table(name = "notification", schema = "public")
public class NotificationModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "user_id")
    private Long userId;

    @Basic
    @Column(name = "title")
    private String title;

    @Basic
    @Column(name = "message")
    private String message;

    @Basic
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Basic
    @Column(name = "is_read")
    private Boolean isRead;

    @Basic
    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
}

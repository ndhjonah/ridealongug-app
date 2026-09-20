package com.ridealongug.backend.models.database;

import com.ridealongug.backend.models.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "driving_licence", schema = "public")
public class DrivingLicenceModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "user_id")
    private Long userId;

    @Basic
    @Column(name = "licence_number", unique = true)
    private String licenceNumber;

    @Basic
    @Column(name = "licence_class")
    private String licenceClass;

    @Basic
    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Basic
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "licence_image_base64", columnDefinition = "TEXT")
    private String licenceImageBase64;

    @Basic
    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @Basic
    @Column(name = "verified_by_super_admin_id")
    private Long verifiedBySuperAdminId;

    @Basic
    @Column(name = "verified_at")
    private Timestamp verifiedAt;

    @Basic
    @Column(name = "rejection_reason")
    private String rejectionReason;
}

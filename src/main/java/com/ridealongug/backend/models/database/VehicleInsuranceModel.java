package com.ridealongug.backend.models.database;

import com.ridealongug.backend.models.enums.InsuranceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "vehicle_insurance", schema = "public")
public class VehicleInsuranceModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Basic
    @Column(name = "insurance_provider")
    private String insuranceProvider;

    @Basic
    @Column(name = "policy_number", unique = true)
    private String policyNumber;

    @Basic
    @Column(name = "insurance_type")
    @Enumerated(EnumType.STRING)
    private InsuranceType insuranceType;

    @Basic
    @Column(name = "coverage_start_date")
    private LocalDate coverageStartDate;

    @Basic
    @Column(name = "coverage_expiry_date")
    private LocalDate coverageExpiryDate;

    @Column(name = "document_base64", columnDefinition = "TEXT")
    private String documentBase64;
}

package com.ridealongug.backend.models.database;

import com.ridealongug.backend.models.enums.MechanicalCondition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "vehicle_inspection", schema = "public")
public class VehicleInspectionModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Basic
    @Column(name = "inspected_by_super_admin_id")
    private Long inspectedBySuperAdminId;

    @Basic
    @Column(name = "inspection_date")
    @CreationTimestamp
    private Timestamp inspectionDate;

    @Basic
    @Column(name = "mechanical_condition")
    @Enumerated(EnumType.STRING)
    private MechanicalCondition mechanicalCondition;

    @Basic
    @Column(name = "notes")
    private String notes;

    @Basic
    @Column(name = "next_inspection_due_date")
    private LocalDate nextInspectionDueDate;
}

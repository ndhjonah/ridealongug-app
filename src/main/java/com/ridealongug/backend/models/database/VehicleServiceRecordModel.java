package com.ridealongug.backend.models.database;

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
@Table(name = "vehicle_service_record", schema = "public")
public class VehicleServiceRecordModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Basic
    @Column(name = "serviced_at_mileage")
    private Long servicedAtMileage;

    @Basic
    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Basic
    @Column(name = "service_type")
    private String serviceType;

    @Basic
    @Column(name = "serviced_by")
    private String servicedBy;

    @Basic
    @Column(name = "notes")
    private String notes;

    @Basic
    @Column(name = "next_service_due_mileage")
    private Long nextServiceDueMileage;
}

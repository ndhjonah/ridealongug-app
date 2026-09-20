package com.ridealongug.backend.models.database;

import com.ridealongug.backend.models.enums.FuelType;
import com.ridealongug.backend.models.enums.MechanicalStatus;
import com.ridealongug.backend.models.enums.OwnershipType;
import com.ridealongug.backend.models.enums.TransmissionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "vehicle", schema = "public")
public class VehicleModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "plate_number", unique = true)
    private String plateNumber;

    @Basic
    @Column(name = "make")
    private String make;

    @Basic
    @Column(name = "model")
    private String model;

    @Basic
    @Column(name = "year")
    private Integer year;

    @Basic
    @Column(name = "category_id")
    private Long categoryId;

    @Basic
    @Column(name = "fuel_type")
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Basic
    @Column(name = "fuel_capacity_litres")
    private Double fuelCapacityLitres;


    @Basic
    @Column(name = "fuel_consumption_per_km")
    private Double fuelConsumptionPerKm;

    @Basic
    @Column(name = "transmission_type")
    @Enumerated(EnumType.STRING)
    private TransmissionType transmissionType;

    @Basic
    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Basic
    @Column(name = "dealer_id")
    private Long dealerId;

    @Basic
    @Column(name = "owner_id")
    private Long ownerId;

    @Basic
    @Column(name = "ownership_type")
    @Enumerated(EnumType.STRING)
    private OwnershipType ownershipType;

    @Basic
    @Column(name = "mechanical_status")
    @Enumerated(EnumType.STRING)
    private MechanicalStatus mechanicalStatus;

    @Basic
    @Column(name = "is_visible")
    private Boolean isVisible;

    @Basic
    @Column(name = "daily_rate")
    private BigDecimal dailyRate;

    @Basic
    @Column(name = "location")
    private String location;

    @Basic
    @Column(name = "current_mileage")
    private Long currentMileage;

    @Basic
    @Column(name = "next_service_due_mileage")
    private Long nextServiceDueMileage;

    @Basic
    @Column(name = "last_service_date")
    private java.time.LocalDate lastServiceDate;

    @Basic
    @Column(name = "verified_by_super_admin_id")
    private Long verifiedBySuperAdminId;

    @Basic
    @Column(name = "verified_at")
    private Timestamp verifiedAt;

    @Basic
    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
}

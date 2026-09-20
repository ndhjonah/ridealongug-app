package com.ridealongug.backend.models.database;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "vehicle_category", schema = "public")
public class VehicleCategoryModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "category_name")
    private String categoryName;

    @Basic
    @Column(name = "requires_driver_license_class")
    private String requiresDriverLicenseClass;

    @Basic
    @Column(name = "base_rate_per_km")
    private BigDecimal baseRatePerKm;

    @Basic
    @Column(name = "base_rate_per_hour")
    private BigDecimal baseRatePerHour;
}

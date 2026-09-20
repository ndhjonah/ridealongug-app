package com.ridealongug.backend.models.database;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "driver_profile", schema = "public")
public class DriverProfileModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "user_id")
    private Long userId;

    @Basic
    @Column(name = "licence_number")
    private String licenceNumber;

    @Basic
    @Column(name = "licence_class")
    private String licenceClass;

    @Basic
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Basic
    @Column(name = "is_available")
    private Boolean isAvailable;

    @Basic
    @Column(name = "rating")
    private Double rating;
}

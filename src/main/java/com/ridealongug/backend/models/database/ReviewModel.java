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
@Table(name = "review", schema = "public")
public class ReviewModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "booking_id", unique = true)
    private Long bookingId;

    @Basic
    @Column(name = "customer_id")
    private Long customerId;

    @Basic
    @Column(name = "vehicle_rating")
    private Integer vehicleRating;

    @Basic
    @Column(name = "driver_rating")
    private Integer driverRating;

    @Basic
    @Column(name = "comment")
    private String comment;

    @Basic
    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
}

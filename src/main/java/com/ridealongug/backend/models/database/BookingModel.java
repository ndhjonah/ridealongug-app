package com.ridealongug.backend.models.database;

import com.ridealongug.backend.models.enums.BookingStatus;
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
@Table(name = "booking", schema = "public")
public class BookingModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "customer_id")
    private Long customerId;

    @Basic
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Basic
    @Column(name = "with_driver")
    private Boolean withDriver;

    @Basic
    @Column(name = "driver_id")
    private Long driverId;

    @Basic
    @Column(name = "pickup_location")
    private String pickupLocation;

    @Basic
    @Column(name = "dropoff_location")
    private String dropoffLocation;

    @Basic
    @Column(name = "pickup_lat")
    private Double pickupLat;

    @Basic
    @Column(name = "pickup_lng")
    private Double pickupLng;

    @Basic
    @Column(name = "dropoff_lat")
    private Double dropoffLat;

    @Basic
    @Column(name = "dropoff_lng")
    private Double dropoffLng;

    @Basic
    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;

    @Basic
    @Column(name = "start_date")
    private Timestamp startDate;

    @Basic
    @Column(name = "end_date")
    private Timestamp endDate;

    @Basic
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @Basic
    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Basic
    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
}

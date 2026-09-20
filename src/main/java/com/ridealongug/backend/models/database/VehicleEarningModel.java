package com.ridealongug.backend.models.database;

import com.ridealongug.backend.models.enums.PayoutStatus;
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
@Table(name = "vehicle_earning", schema = "public")
public class VehicleEarningModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "booking_id", unique = true)
    private Long bookingId;

    @Basic
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Basic
    @Column(name = "owner_id")
    private Long ownerId;

    @Basic
    @Column(name = "gross_amount")
    private BigDecimal grossAmount;

    @Basic
    @Column(name = "commission_percentage")
    private Double commissionPercentage;

    @Basic
    @Column(name = "owner_amount")
    private BigDecimal ownerAmount;

    @Basic
    @Column(name = "platform_amount")
    private BigDecimal platformAmount;

    @Basic
    @Column(name = "payout_status")
    @Enumerated(EnumType.STRING)
    private PayoutStatus payoutStatus;

    @Basic
    @Column(name = "paid_at")
    private Timestamp paidAt;

    @Basic
    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
}

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
@Table(name = "discount_coupon", schema = "public")
public class DiscountCouponModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "code", unique = true)
    private String code;

    @Basic
    @Column(name = "discount_percentage")
    private Double discountPercentage;

    @Basic
    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Basic
    @Column(name = "valid_to")
    private LocalDate validTo;

    @Basic
    @Column(name = "max_uses")
    private Integer maxUses;

    @Basic
    @Column(name = "times_used")
    @Builder.Default
    private Integer timesUsed = 0;

    @Basic
    @Column(name = "is_active")
    private Boolean isActive;
}

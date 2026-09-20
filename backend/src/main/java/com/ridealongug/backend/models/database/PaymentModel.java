package com.ridealongug.backend.models.database;

import com.ridealongug.backend.models.enums.PaymentMethod;
import com.ridealongug.backend.models.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payment", schema = "public")
public class PaymentModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "booking_id")
    private Long bookingId;

    @Basic
    @Column(name = "amount")
    private BigDecimal amount;

    @Basic
    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Basic
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Basic
    @Column(name = "transaction_ref")
    private String transactionRef;

    @Basic
    @Column(name = "paid_at")
    private Timestamp paidAt;
}

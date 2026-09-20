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
@Table(name = "dealer", schema = "public")
public class DealerModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;



    @Basic
    @Column(name = "user_id")
    private Long userId;

    @Basic
    @Column(name = "dealer_name")
    private String dealerName;

    @Basic
    @Column(name = "contact_person")
    private String contactPerson;

    @Basic
    @Column(name = "phone_number")
    private String phoneNumber;

    @Basic
    @Column(name = "email")
    private String email;

    @Basic
    @Column(name = "location")
    private String location;

    @Basic
    @Column(name = "bond_registration_number")
    private String bondRegistrationNumber;

    @Basic
    @Column(name = "is_active")
    private Boolean isActive;

    @Basic
    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;
}

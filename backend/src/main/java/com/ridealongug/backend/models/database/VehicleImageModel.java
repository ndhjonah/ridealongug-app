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
@Table(name = "vehicle_image", schema = "public")
public class VehicleImageModel {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "image_base64", columnDefinition = "TEXT")
    private String imageBase64;

    @Basic
    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Basic
    @Column(name = "uploaded_at")
    @CreationTimestamp
    private Timestamp uploadedAt;
}

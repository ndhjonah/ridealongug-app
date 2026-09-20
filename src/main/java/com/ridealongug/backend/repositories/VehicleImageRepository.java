package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.VehicleImageModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleImageRepository extends JetRepository<VehicleImageModel, Long> {
    List<VehicleImageModel> findAllByVehicleId(Long vehicleId);
}

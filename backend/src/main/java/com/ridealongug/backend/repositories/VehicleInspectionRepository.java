package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.VehicleInspectionModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleInspectionRepository extends JetRepository<VehicleInspectionModel, Long> {
    List<VehicleInspectionModel> findAllByVehicleId(Long vehicleId);
    List<VehicleInspectionModel> findAllByVehicleIdOrderByInspectionDateDesc(Long vehicleId);
    Optional<VehicleInspectionModel> findFirstByVehicleIdOrderByInspectionDateDesc(Long vehicleId);
}

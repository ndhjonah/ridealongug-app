package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.VehicleServiceRecordModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleServiceRecordRepository extends JetRepository<VehicleServiceRecordModel, Long> {
    List<VehicleServiceRecordModel> findAllByVehicleId(Long vehicleId);
    List<VehicleServiceRecordModel> findAllByVehicleIdOrderByServiceDateDesc(Long vehicleId);
    Optional<VehicleServiceRecordModel> findFirstByVehicleIdOrderByServiceDateDesc(Long vehicleId);
}

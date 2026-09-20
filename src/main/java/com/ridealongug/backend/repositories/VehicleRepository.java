package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.VehicleModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JetRepository<VehicleModel, Long>, JpaSpecificationExecutor<VehicleModel> {
    Optional<VehicleModel> findFirstByPlateNumber(String plateNumber);
    List<VehicleModel> findAllByIsVisibleTrue();
}

package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.VehicleInsuranceModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleInsuranceRepository extends JetRepository<VehicleInsuranceModel, Long> {
    List<VehicleInsuranceModel> findAllByVehicleId(Long vehicleId);
    Optional<VehicleInsuranceModel> findFirstByVehicleIdAndCoverageExpiryDateGreaterThanEqual(Long vehicleId, LocalDate today);
}

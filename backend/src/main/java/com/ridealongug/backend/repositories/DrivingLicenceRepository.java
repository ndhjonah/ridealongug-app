package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.DrivingLicenceModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrivingLicenceRepository extends JetRepository<DrivingLicenceModel, Long> {
    Optional<DrivingLicenceModel> findFirstByUserId(Long userId);
    Optional<DrivingLicenceModel> findFirstByLicenceNumber(String licenceNumber);
}

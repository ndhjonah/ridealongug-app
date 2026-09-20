package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.DriverProfileModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverProfileRepository extends JetRepository<DriverProfileModel, Long> {
    Optional<DriverProfileModel> findFirstByUserId(Long userId);
    List<DriverProfileModel> findAllByIsAvailableTrue();
}

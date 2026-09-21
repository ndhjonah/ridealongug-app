package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.VehicleCategoryModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleCategoryRepository extends JetRepository<VehicleCategoryModel, Long> {
}

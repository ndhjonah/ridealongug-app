package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.DealerModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealerRepository extends JetRepository<DealerModel, Long>, JpaSpecificationExecutor<DealerModel> {
    Optional<DealerModel> findFirstByUserId(Long userId);
}

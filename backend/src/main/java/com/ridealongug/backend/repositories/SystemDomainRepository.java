package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.SystemDomainModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemDomainRepository extends JetRepository<SystemDomainModel, Long> {
}

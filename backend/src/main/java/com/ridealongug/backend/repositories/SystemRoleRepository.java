package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.SystemRoleModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemRoleRepository extends JetRepository<SystemRoleModel, Long> {
    Optional<SystemRoleModel> findFirstByRoleCode(String code);
}

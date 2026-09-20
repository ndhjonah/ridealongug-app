package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.SystemPermissionModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemPermissionRepository extends JetRepository<SystemPermissionModel, Long> {
    Optional<SystemPermissionModel> findFirstByPermissionCode(String permissionCode);
}

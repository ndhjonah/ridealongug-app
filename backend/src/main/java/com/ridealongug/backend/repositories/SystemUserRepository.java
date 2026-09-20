package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.SystemUserModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemUserRepository extends JetRepository<SystemUserModel, Long> {
    SystemUserModel findFirstByUsername(String username);
    Optional<SystemUserModel> findFirstByUsernameOrEmail(String username, String email);
}

package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.PasswordResetTokenModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JetRepository<PasswordResetTokenModel, Long> {
    Optional<PasswordResetTokenModel> findFirstByToken(String token);
}

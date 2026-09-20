package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.ReviewModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JetRepository<ReviewModel, Long> {
    Optional<ReviewModel> findFirstByBookingId(Long bookingId);
    List<ReviewModel> findAllByCustomerId(Long customerId);
}

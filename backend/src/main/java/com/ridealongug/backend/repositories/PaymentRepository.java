package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.PaymentModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JetRepository<PaymentModel, Long> {
    List<PaymentModel> findAllByBookingId(Long bookingId);
}

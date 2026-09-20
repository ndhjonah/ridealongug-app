package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.DiscountCouponModel;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscountCouponRepository extends JetRepository<DiscountCouponModel, Long> {
    Optional<DiscountCouponModel> findFirstByCodeAndIsActiveTrue(String code);
}

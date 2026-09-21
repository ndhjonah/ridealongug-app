package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.VehicleEarningModel;
import com.ridealongug.backend.models.enums.PayoutStatus;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleEarningRepository extends JetRepository<VehicleEarningModel, Long> {
    List<VehicleEarningModel> findAllByOwnerId(Long ownerId);
    List<VehicleEarningModel> findAllByOwnerIdAndPayoutStatus(Long ownerId, PayoutStatus payoutStatus);
    List<VehicleEarningModel> findAllByPayoutStatus(PayoutStatus payoutStatus);
    Optional<VehicleEarningModel> findFirstByBookingId(Long bookingId);
}

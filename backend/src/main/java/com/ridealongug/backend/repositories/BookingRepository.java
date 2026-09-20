package com.ridealongug.backend.repositories;

import com.ridealongug.backend.models.database.BookingModel;
import com.ridealongug.backend.models.enums.BookingStatus;
import com.ridealongug.backend.models.jpahelpers.repository.JetRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface BookingRepository extends JetRepository<BookingModel, Long>, JpaSpecificationExecutor<BookingModel> {
    List<BookingModel> findAllByCustomerId(Long customerId);
    List<BookingModel> findAllByVehicleId(Long vehicleId);
    List<BookingModel> findAllByVehicleIdAndStatusIn(Long vehicleId, List<BookingStatus> statuses);
    List<BookingModel> findAllByVehicleIdAndEndDateAfterAndStartDateBefore(Long vehicleId, Timestamp start, Timestamp end);
    List<BookingModel> findAllByDriverId(Long driverId);
    List<BookingModel> findAllByVehicleIdInAndStatusIn(List<Long> vehicleIds, List<BookingStatus> statuses);
}

package com.ridealongug.backend.services.vehicle;

import com.ridealongug.backend.models.database.VehicleInsuranceModel;
import com.ridealongug.backend.models.database.VehicleModel;
import com.ridealongug.backend.models.enums.MechanicalStatus;
import com.ridealongug.backend.repositories.VehicleInsuranceRepository;
import com.ridealongug.backend.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleAvailabilityService {

    private final VehicleRepository vehicleRepository;
    private final VehicleInsuranceRepository vehicleInsuranceRepository;

    public boolean isEligibleForHire(Long vehicleId) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));
        return isEligibleForHire(vehicle);
    }

    public boolean isEligibleForHire(VehicleModel vehicle) {
        if (vehicle.getMechanicalStatus() != MechanicalStatus.APPROVED) {
            return false;
        }
        if (!hasActiveInsurance(vehicle.getId())) {
            return false;
        }
        return !isOverdueForService(vehicle);
    }

    public boolean hasActiveInsurance(Long vehicleId) {
        List<VehicleInsuranceModel> policies = vehicleInsuranceRepository.findAllByVehicleId(vehicleId);
        LocalDate today = LocalDate.now();
        return policies.stream().anyMatch(p ->
                p.getCoverageStartDate() != null
                        && p.getCoverageExpiryDate() != null
                        && !today.isBefore(p.getCoverageStartDate())
                        && !today.isAfter(p.getCoverageExpiryDate())
        );
    }

    public boolean isOverdueForService(VehicleModel vehicle) {
        if (vehicle.getNextServiceDueMileage() == null || vehicle.getCurrentMileage() == null) {


            return false;
        }
        return vehicle.getCurrentMileage() >= vehicle.getNextServiceDueMileage();
    }



    public void refreshVisibility(Long vehicleId) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));
        vehicle.setIsVisible(isEligibleForHire(vehicle));
        vehicleRepository.save(vehicle);
    }
}

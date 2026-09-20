package com.ridealongug.backend.services.booking;

import com.ridealongug.backend.models.database.VehicleCategoryModel;
import com.ridealongug.backend.models.database.VehicleModel;
import com.ridealongug.backend.repositories.VehicleCategoryRepository;
import com.ridealongug.backend.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PricingService {

    @Value("${FUEL_PRICE_PER_LITRE:5500}")
    private Double fuelPricePerLitre;

    private static final BigDecimal DRIVER_FEE_PER_DAY = BigDecimal.valueOf(50000);

    private final VehicleRepository vehicleRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;

    public BigDecimal calculateCost(Long vehicleId, Double estimatedDistanceKm, Double estimatedDurationHours,
                                    boolean withDriver, int numberOfDays) {
        VehicleModel vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Vehicle not found"));
        VehicleCategoryModel category = vehicleCategoryRepository.findById(vehicle.getCategoryId())
                .orElseThrow(() -> new IllegalStateException("Vehicle category not found"));

        double distanceKm = estimatedDistanceKm == null ? 0 : estimatedDistanceKm;

        BigDecimal distanceCost = safe(category.getBaseRatePerKm())
                .multiply(BigDecimal.valueOf(distanceKm));

        BigDecimal timeCost = safe(category.getBaseRatePerHour())
                .multiply(BigDecimal.valueOf(estimatedDurationHours == null ? 0 : estimatedDurationHours));

        double consumptionPerKm = vehicle.getFuelConsumptionPerKm() == null ? 0 : vehicle.getFuelConsumptionPerKm();
        BigDecimal fuelCost = BigDecimal.valueOf(distanceKm)
                .multiply(BigDecimal.valueOf(consumptionPerKm))
                .multiply(BigDecimal.valueOf(fuelPricePerLitre));

        BigDecimal driverFee = withDriver
                ? DRIVER_FEE_PER_DAY.multiply(BigDecimal.valueOf(Math.max(numberOfDays, 1)))
                : BigDecimal.ZERO;

        return distanceCost.add(timeCost).add(fuelCost).add(driverFee)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

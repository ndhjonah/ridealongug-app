package com.ridealongug.backend.config;

import com.ridealongug.backend.models.database.*;
import com.ridealongug.backend.models.enums.*;
import com.ridealongug.backend.repositories.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder {

    @Value("${SEED_DEMO_DATA:true}")
    private Boolean seedDemoData;

    private final VehicleCategoryRepository categoryRepository;
    private final DealerRepository dealerRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleInspectionRepository inspectionRepository;
    private final VehicleInsuranceRepository insuranceRepository;
    private final VehicleServiceRecordRepository serviceRecordRepository;

    @PostConstruct
    public void seed() {
        if (Boolean.FALSE.equals(seedDemoData)) {
            log.info("SEED_DEMO_DATA is false - skipping demo data seeding.");
            return;
        }
        if (!categoryRepository.findAll().isEmpty()) {
            log.info("Demo data already present - skipping seeding.");
            return;
        }

        log.info("Seeding demo data for RideAlongUG...");

        VehicleCategoryModel sedan = categoryRepository.save(VehicleCategoryModel.builder()
                .categoryName("Sedan")
                .requiresDriverLicenseClass("B")
                .baseRatePerKm(BigDecimal.valueOf(1500))
                .baseRatePerHour(BigDecimal.valueOf(5000))
                .build());

        VehicleCategoryModel suv = categoryRepository.save(VehicleCategoryModel.builder()
                .categoryName("SUV")
                .requiresDriverLicenseClass("B")
                .baseRatePerKm(BigDecimal.valueOf(2200))
                .baseRatePerHour(BigDecimal.valueOf(8000))
                .build());

        VehicleCategoryModel pickup = categoryRepository.save(VehicleCategoryModel.builder()
                .categoryName("Pickup")
                .requiresDriverLicenseClass("C")
                .baseRatePerKm(BigDecimal.valueOf(2000))
                .baseRatePerHour(BigDecimal.valueOf(7000))
                .build());

        VehicleCategoryModel tractor = categoryRepository.save(VehicleCategoryModel.builder()
                .categoryName("Tractor")
                .requiresDriverLicenseClass("F")
                .baseRatePerKm(BigDecimal.valueOf(3000))
                .baseRatePerHour(BigDecimal.valueOf(15000))
                .build());

        VehicleCategoryModel sugarcaneTruck = categoryRepository.save(VehicleCategoryModel.builder()
                .categoryName("Sugarcane Truck")
                .requiresDriverLicenseClass("D")
                .baseRatePerKm(BigDecimal.valueOf(3500))
                .baseRatePerHour(BigDecimal.valueOf(18000))
                .build());

        DealerModel dealer = dealerRepository.save(DealerModel.builder()
                .dealerName("Kampala Car Bond Ltd")
                .contactPerson("Moses Okello")
                .phoneNumber("+256700123456")
                .email("info@kampalacarbond.ug")
                .location("Kampala, Ntinda")
                .bondRegistrationNumber("UG-BOND-2024-0091")
                .isActive(true)
                .build());

        seedVehicle("UBA 123A", "Toyota", "Premio", 2018, sedan.getId(), null,
                OwnershipType.PLATFORM_OWNED, FuelType.PETROL, 50.0, 0.08, TransmissionType.AUTOMATIC, 5,
                BigDecimal.valueOf(120000), "Kampala, Nakawa");

        seedVehicle("UBB 456B", "Toyota", "Land Cruiser Prado", 2020, suv.getId(), dealer.getId(),
                OwnershipType.DEALER_SUPPLIED, FuelType.DIESEL, 87.0, 0.12, TransmissionType.AUTOMATIC, 7,
                BigDecimal.valueOf(250000), "Kampala, Ntinda");

        seedVehicle("UBC 789C", "Isuzu", "D-Max", 2019, pickup.getId(), dealer.getId(),
                OwnershipType.DEALER_SUPPLIED, FuelType.DIESEL, 76.0, 0.10, TransmissionType.MANUAL, 5,
                BigDecimal.valueOf(180000), "Mukono");

        seedVehicle("UBD 321D", "Massey Ferguson", "MF 385", 2015, tractor.getId(), null,
                OwnershipType.PLATFORM_OWNED, FuelType.DIESEL, 120.0, 0.20, TransmissionType.MANUAL, null,
                BigDecimal.valueOf(300000), "Jinja");

        seedVehicle("UBE 654E", "Howo", "Sinotruk Cane Hauler", 2017, sugarcaneTruck.getId(), dealer.getId(),
                OwnershipType.DEALER_SUPPLIED, FuelType.DIESEL, 200.0, 0.35, TransmissionType.MANUAL, null,
                BigDecimal.valueOf(400000), "Jinja, Lugazi");

        log.info("Demo data seeding complete: 5 categories, 1 dealer, 5 vehicles (approved, insured, service-ready).");
    }

    private void seedVehicle(String plate, String make, String model, int year, Long categoryId, Long dealerId,
                             OwnershipType ownershipType, FuelType fuelType, double fuelCapacity, double fuelConsumptionPerKm,
                             TransmissionType transmission, Integer seats, BigDecimal dailyRate, String location) {

        VehicleModel vehicle = VehicleModel.builder()
                .plateNumber(plate)
                .make(make)
                .model(model)
                .year(year)
                .categoryId(categoryId)
                .dealerId(dealerId)
                .ownershipType(ownershipType)
                .fuelType(fuelType)
                .fuelCapacityLitres(fuelCapacity)
                .fuelConsumptionPerKm(fuelConsumptionPerKm)
                .transmissionType(transmission)
                .seatingCapacity(seats)
                .dailyRate(dailyRate)
                .location(location)
                .mechanicalStatus(MechanicalStatus.PENDING_VERIFICATION)
                .isVisible(false)
                .currentMileage(15000L)
                .build();
        vehicle = vehicleRepository.save(vehicle);


        inspectionRepository.save(VehicleInspectionModel.builder()
                .vehicleId(vehicle.getId())
                .mechanicalCondition(MechanicalCondition.GOOD)
                .notes("Initial demo inspection - all systems good")
                .nextInspectionDueDate(LocalDate.now().plusMonths(6))
                .build());
        vehicle.setMechanicalStatus(MechanicalStatus.APPROVED);


        insuranceRepository.save(VehicleInsuranceModel.builder()
                .vehicleId(vehicle.getId())
                .insuranceProvider("Jubilee Insurance Uganda")
                .policyNumber("POL-" + plate.replace(" ", ""))
                .insuranceType(InsuranceType.THIRD_PARTY)
                .coverageStartDate(LocalDate.now().minusMonths(1))
                .coverageExpiryDate(LocalDate.now().plusMonths(11))
                .build());


        serviceRecordRepository.save(VehicleServiceRecordModel.builder()
                .vehicleId(vehicle.getId())
                .servicedAtMileage(15000L)
                .serviceDate(LocalDate.now().minusDays(10))
                .serviceType("Full service")
                .servicedBy("RideAlongUG Garage")
                .nextServiceDueMileage(20000L)
                .build());
        vehicle.setNextServiceDueMileage(20000L);
        vehicle.setLastServiceDate(LocalDate.now().minusDays(10));


        vehicle.setIsVisible(true);
        vehicleRepository.save(vehicle);
    }
}

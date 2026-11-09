package com.starkvms.vmsbackend.infrastructure.persistence;

import com.starkvms.vmsbackend.TestAuthSupport;
import com.starkvms.vmsbackend.infrastructure.persistence.entity.VehicleEntity;
import com.starkvms.vmsbackend.infrastructure.persistence.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class VehicleRepositoryTest {

    @DynamicPropertySource
    static void authProperties(DynamicPropertyRegistry registry) {
        TestAuthSupport.registerProperties(registry);
    }

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void databaseRejectsDuplicateVehicleNumbers() {
        vehicleRepository.deleteAll();
        vehicleRepository.saveAndFlush(vehicle("ABC-123"));

        assertThrows(DataIntegrityViolationException.class,
                () -> vehicleRepository.saveAndFlush(vehicle("ABC-123")));

        vehicleRepository.deleteAll();
    }

    @Test
    void generatedPrimaryKeyIsAssigned() {
        VehicleEntity saved = vehicleRepository.saveAndFlush(vehicle("XYZ-789"));
        assertNotNull(saved.getId());
        vehicleRepository.deleteAll();
    }

    private VehicleEntity vehicle(String vehicleNumber) {
        return new VehicleEntity(vehicleNumber, "Stark Group", "Truck",
                "Volvo", "FH", "Active");
    }
}

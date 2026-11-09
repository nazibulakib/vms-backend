package com.starkvms.vmsbackend.application.service;

import com.starkvms.vmsbackend.api.dto.CreateVehicleRequest;
import com.starkvms.vmsbackend.api.dto.VehicleResponse;
import com.starkvms.vmsbackend.infrastructure.persistence.entity.VehicleEntity;
import com.starkvms.vmsbackend.infrastructure.persistence.repository.VehicleRepository;
import com.starkvms.vmsbackend.shared.exception.DuplicateVehicleNumberException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

class VehicleServiceTest {

    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);
    private final VehicleService vehicleService = new VehicleService(vehicleRepository);

    @Test
    void rejectsDuplicateVehicleNumberWithoutSaving() {
        when(vehicleRepository.existsByVehicleNumber("ABC-123")).thenReturn(true);

        DuplicateVehicleNumberException exception = assertThrows(
                DuplicateVehicleNumberException.class,
                () -> vehicleService.registerVehicle(request(" abc-123 ")));

        assertEquals("A vehicle with this vehicle number already exists.", exception.getMessage());
        verify(vehicleRepository).existsByVehicleNumber("ABC-123");
        verify(vehicleRepository, never()).saveAndFlush(any(VehicleEntity.class));
    }

    @Test
    void registersVehicleWhenNumberIsAvailable() {
        when(vehicleRepository.existsByVehicleNumber("ABC-123")).thenReturn(false);
        when(vehicleRepository.saveAndFlush(any(VehicleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse registered = vehicleService.registerVehicle(request(" abc-123 "));

        assertAll(
                () -> assertEquals("ABC-123", registered.vehicleNumber()),
                () -> assertEquals("Stark Group", registered.company()),
                () -> assertEquals("Truck", registered.type()),
                () -> assertEquals("Volvo", registered.brand()),
                () -> assertEquals("FH", registered.model()),
                () -> assertEquals("Active", registered.status()));
        verify(vehicleRepository).existsByVehicleNumber("ABC-123");
        verify(vehicleRepository).saveAndFlush(any(VehicleEntity.class));
    }

    @Test
    void translatesDatabaseUniqueConstraintConflict() {
        when(vehicleRepository.existsByVehicleNumber("ABC-123")).thenReturn(false);
        when(vehicleRepository.saveAndFlush(any(VehicleEntity.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThrows(DuplicateVehicleNumberException.class,
                () -> vehicleService.registerVehicle(request("ABC-123")));
    }

    private CreateVehicleRequest request(String vehicleNumber) {
        return new CreateVehicleRequest(vehicleNumber, "Stark Group", "Truck",
                "Volvo", "FH", "Active");
    }
}

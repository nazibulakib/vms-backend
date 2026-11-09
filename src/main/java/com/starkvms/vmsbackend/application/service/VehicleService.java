package com.starkvms.vmsbackend.application.service;

import com.starkvms.vmsbackend.api.dto.CreateVehicleRequest;
import com.starkvms.vmsbackend.api.dto.VehicleResponse;
import com.starkvms.vmsbackend.infrastructure.persistence.entity.VehicleEntity;
import com.starkvms.vmsbackend.infrastructure.persistence.repository.VehicleRepository;
import com.starkvms.vmsbackend.shared.exception.DuplicateVehicleNumberException;
import com.starkvms.vmsbackend.shared.exception.VehicleNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public VehicleResponse registerVehicle(CreateVehicleRequest request) {
        String normalizedVehicleNumber = normalizeVehicleNumber(request.vehicleNumber());
        ensureVehicleNumberAvailable(normalizedVehicleNumber, null);

        VehicleEntity vehicle = new VehicleEntity(
                normalizedVehicleNumber, clean(request.company()), clean(request.type()),
                clean(request.brand()), clean(request.model()), clean(request.status()));

        try {
            return VehicleResponse.from(vehicleRepository.saveAndFlush(vehicle));
        } catch (DataIntegrityViolationException exception) {
            // The database constraint also protects concurrent registrations.
            throw new DuplicateVehicleNumberException(exception);
        }
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> listVehicles() {
        return vehicleRepository.findAll(Sort.by(Sort.Direction.ASC, "vehicleNumber"))
                .stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(Long id) {
        return VehicleResponse.from(findVehicle(id));
    }

    public VehicleResponse updateVehicle(Long id, CreateVehicleRequest request) {
        VehicleEntity vehicle = findVehicle(id);
        String normalizedVehicleNumber = normalizeVehicleNumber(request.vehicleNumber());
        ensureVehicleNumberAvailable(normalizedVehicleNumber, id);
        vehicle.updateDetails(normalizedVehicleNumber, clean(request.company()),
                clean(request.type()), clean(request.brand()), clean(request.model()),
                clean(request.status()));

        try {
            return VehicleResponse.from(vehicleRepository.saveAndFlush(vehicle));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateVehicleNumberException(exception);
        }
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.delete(findVehicle(id));
    }

    private VehicleEntity findVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    private void ensureVehicleNumberAvailable(String vehicleNumber, Long currentId) {
        boolean exists = currentId == null
                ? vehicleRepository.existsByVehicleNumber(vehicleNumber)
                : vehicleRepository.existsByVehicleNumberAndIdNot(vehicleNumber, currentId);
        if (exists) {
            throw new DuplicateVehicleNumberException();
        }
    }

    private String clean(String value) {
        return value.trim();
    }

    private String normalizeVehicleNumber(String vehicleNumber) {
        return vehicleNumber.trim().toUpperCase(Locale.ROOT);
    }
}

package com.starkvms.vmsbackend.api.dto;

import com.starkvms.vmsbackend.infrastructure.persistence.entity.VehicleEntity;

public record VehicleResponse(
        Long id,
        String vehicleNumber,
        String company,
        String type,
        String brand,
        String model,
        String status) {

    public static VehicleResponse from(VehicleEntity vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getCompany(),
                vehicle.getType(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getStatus());
    }
}

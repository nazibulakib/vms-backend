package com.starkvms.vmsbackend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateVehicleRequest(
        @NotBlank(message = "Vehicle number is required")
        @Size(max = 64, message = "Vehicle number must be 64 characters or fewer")
        String vehicleNumber,

        @NotBlank(message = "Company is required")
        @Size(max = 120, message = "Company must be 120 characters or fewer")
        String company,

        @NotBlank(message = "Type is required")
        @Size(max = 64, message = "Type must be 64 characters or fewer")
        String type,

        @NotBlank(message = "Brand is required")
        @Size(max = 100, message = "Brand must be 100 characters or fewer")
        String brand,

        @NotBlank(message = "Model is required")
        @Size(max = 100, message = "Model must be 100 characters or fewer")
        String model,

        @NotBlank(message = "Status is required")
        @Size(max = 64, message = "Status must be 64 characters or fewer")
        String status) {
}

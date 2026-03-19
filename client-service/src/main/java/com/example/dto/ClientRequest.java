package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ClientRequest(
        @NotNull(message = "orgUnitId is required")
        UUID orgUnitId,
        @NotBlank(message = "clientName is required")
        @Size(max = 255, message = "clientName must be 255 characters or fewer")
        String clientName,
        @Size(max = 255, message = "industry must be 255 characters or fewer")
        String industry,
        @NotBlank(message = "status is required")
        @Size(max = 64, message = "status must be 64 characters or fewer")
        String status
) {
}

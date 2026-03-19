package com.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrgUnitRequest(
        @NotBlank(message = "orgUnitName is required")
        @Size(max = 255, message = "orgUnitName must be 255 characters or fewer")
        String orgUnitName,
        @NotBlank(message = "status is required")
        @Size(max = 64, message = "status must be 64 characters or fewer")
        String status
) {
}

package mh.backend.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserRequest(
        @NotNull(message = "orgUnitId is required")
        UUID orgUnitId,
        @NotBlank(message = "fullName is required")
        @Size(max = 255, message = "fullName must be 255 characters or fewer")
        String fullName,
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 255, message = "email must be 255 characters or fewer")
        String email,
        @NotBlank(message = "status is required")
        @Size(max = 64, message = "status must be 64 characters or fewer")
        String status
) {
}

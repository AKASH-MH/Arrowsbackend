package mh.backend.userservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        UUID orgUnitId,
        String orgUnitName,
        String fullName,
        String email,
        String status,
        LocalDateTime createdAt
) {
}

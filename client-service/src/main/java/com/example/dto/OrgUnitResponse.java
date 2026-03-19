package com.example.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrgUnitResponse(
        UUID orgUnitId,
        String orgUnitName,
        String status,
        LocalDateTime createdAt
) {
}

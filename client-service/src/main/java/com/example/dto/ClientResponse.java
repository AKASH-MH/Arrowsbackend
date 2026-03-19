package com.example.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClientResponse(
        UUID clientId,
        UUID orgUnitId,
        String orgUnitName,
        String clientName,
        String industry,
        String status,
        LocalDateTime createdAt
) {
}

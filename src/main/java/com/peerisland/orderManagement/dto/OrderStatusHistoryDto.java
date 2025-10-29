package com.peerisland.orderManagement.dto;

import java.time.OffsetDateTime;

/**
 * DTO for order status history records.
 */
public record OrderStatusHistoryDto(
    Long id,
    String previousStatus,
    String newStatus,
    String changedBy,
    OffsetDateTime changedAt
) {}

package org.example.controller.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CartItemsResponse(
    Long productId,
    int quantity,
    BigDecimal lineTotal
) {}

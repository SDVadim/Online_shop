package org.example.controller.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponse(
    Long id,
    String name,
    BigDecimal price,
    Long category_id
) {}

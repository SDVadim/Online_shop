package org.example.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponse(
    @NotNull Long id,
    @NotBlank String name,
    @Min(0) BigDecimal price,
    @NotNull Long category_id
) {}

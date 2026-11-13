package org.example.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank String name,
    @Min(0) BigDecimal price,
    @NotNull Long categoryId
) {}
package org.example.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank @Size(max = 50) String name,
    @Min(0) @NotNull BigDecimal price,
    @NotNull Long categoryId
) {}
package org.example.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductResponse(
    @NotNull Long id,
    @NotBlank String name,
    @Min(0) BigDecimal price
) {}

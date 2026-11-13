package org.example.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
    @NotBlank String name
) {}

package org.example.controller.dto;

import lombok.Builder;

@Builder
public record CategoryResponse(
    Long id,
    String name
) {}

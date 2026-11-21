package org.example.controller.dto;

import lombok.Builder;

@Builder
public record CartResponse(Long productId, int quantity) {}

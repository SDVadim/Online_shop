package org.example.controller.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartResponse(List<CartResponse> items, BigDecimal totalPrice) {}

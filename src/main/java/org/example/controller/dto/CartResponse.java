package org.example.controller.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartResponse(List<CartItemsResponse> items, BigDecimal totalPrice) {}

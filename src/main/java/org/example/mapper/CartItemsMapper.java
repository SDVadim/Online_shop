package org.example.mapper;

import org.example.controller.dto.CartItemsResponse;
import org.example.repository.Entities.CartItemEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartItemsMapper {
    public CartItemsResponse toResponse(CartItemEntity entity) {
        return CartItemsResponse.builder()
            .quantity(entity.getQuantity())
            .productId(entity.getProduct().getId())
            .lineTotal(entity.getProduct().getPrice().multiply(new BigDecimal(entity.getQuantity())))
            .build();
    }
}

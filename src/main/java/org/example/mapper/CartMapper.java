package org.example.mapper;

import org.example.controller.dto.CartResponse;
import org.example.repository.Entities.CartEntity;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {
    public CartResponse toResponse(CartEntity cartEntity) {
        return CartResponse.builder()
            .productId(cartEntity.getProduct().getId())
            .quantity(cartEntity.getQuantity())
            .build();
    }
}

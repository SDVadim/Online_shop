package org.example.mapper;

import org.example.controller.dto.ProductRequest;
import org.example.controller.dto.ProductResponse;
import org.example.repository.Entities.CategoryEntity;
import org.example.repository.Entities.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductResponse toResponse(ProductEntity entity) {
        return ProductResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .category_id(entity.getCategory().getId())
            .price(entity.getPrice())
            .build();
    }

    public ProductEntity toEntity(ProductRequest productResponse, CategoryEntity category) {
        return ProductEntity.builder()
            .category(category)
            .name(productResponse.name())
            .price(productResponse.price())
            .build();
    }
}

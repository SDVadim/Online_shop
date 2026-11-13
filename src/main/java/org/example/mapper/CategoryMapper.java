package org.example.mapper;

import org.example.controller.dto.CategoryRequest;
import org.example.controller.dto.CategoryResponse;
import org.example.repository.Entities.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryEntity toEntity(CategoryRequest categoryResponse) {
        return CategoryEntity.builder()
            .name(categoryResponse.name())
            .build();
    }

    public CategoryResponse toResponse(CategoryEntity entity) {
        return CategoryResponse.builder()
            .name(entity.getName())
            .id(entity.getId())
            .build();
    }
}

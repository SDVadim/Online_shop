package org.example.mapper;

import org.example.controller.dto.UserRequest;
import org.example.controller.dto.UserResponse;
import org.example.repository.Entities.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UserMapper {
    public UserEntity toEntity(UserRequest request) {
        return UserEntity.builder()
            .name(request.name())
            .email(request.email().strip().toLowerCase(Locale.ROOT))
            .build();
    }

    public UserResponse toResponse(UserEntity entity) {
        return UserResponse.builder()
            .id(entity.getId())
            .name(entity.getName())
            .role(entity.getRole())
            .build();
    }
}

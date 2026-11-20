package org.example.controller.dto;

import lombok.Builder;
import org.example.repository.Entities.UserEntity;

@Builder
public record UserResponse(
    Long id,
    String name,
    UserEntity.UsersRole role
) {}

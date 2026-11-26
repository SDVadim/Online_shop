package org.example.repository;

import org.example.repository.Entities.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    void deleteAllByUserId(Long id);

    List<CartItemEntity> findCartEntitiesByUserId(Long userId);

    Optional<CartItemEntity> findCartEntitiesByUserIdAndProductId(Long userId, Long productId);

    void deleteAllByProductId(Long id);
}

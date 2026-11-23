package org.example.repository;

import org.example.repository.Entities.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartEntity, Long> {
    void deleteAllByUserId(Long id);

    List<CartEntity> findCartEntitiesByUserId(Long userId);

    Optional<CartEntity> findCartEntitiesByUserIdAndProductId(Long userId, Long productId);

    void deleteAllByProductId(Long id);
}

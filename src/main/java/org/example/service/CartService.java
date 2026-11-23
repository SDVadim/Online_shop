package org.example.service;

import org.example.controller.dto.CartResponse;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.ResourceNotFoundException.Resource;
import org.example.mapper.CartMapper;
import org.example.repository.CartItemRepository;
import org.example.repository.Entities.CartEntity;
import org.example.repository.Entities.ProductEntity;
import org.example.repository.Entities.UserEntity;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;

    public CartService(
        CartItemRepository cartItemRepository,
        UserRepository userRepository,
        CartMapper cartMapper,
        ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.cartMapper = cartMapper;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CartResponse> findCart(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist")
        );
        return cartItemRepository.findCartEntitiesByUserId(user.getId())
            .stream()
            .map(cartMapper::toResponse)
            .toList();
    }

    @Transactional
    public CartResponse incrementProduct(String email, Long productId) {
        UserEntity user = userRepository.findByEmailForCartUpdate(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        Optional<CartEntity> cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(
            user.getId(),
            productId
        );
        if (cartEntity.isEmpty()) {
            ProductEntity product = productRepository.findById(productId).orElseThrow(() ->
                new ResourceNotFoundException(Resource.PRODUCT, productId)
            );
            CartEntity entity = CartEntity.builder()
                .user(user)
                .product(product)
                .quantity(1)
                .build();
            CartEntity savedCartEntity = cartItemRepository.save(entity);
            return cartMapper.toResponse(savedCartEntity);
        } else {
            CartEntity entity = cartEntity.get();
            entity.increment();
            return cartMapper.toResponse(entity);
        }
    }

    @Transactional
    public Optional<CartResponse> decrementProduct(String email, Long productId) {
        UserEntity user = userRepository.findByEmailForCartUpdate(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        CartEntity cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(
            user.getId(),
            productId
        ).orElseThrow(() ->
            new ResourceNotFoundException("Product with ID: " + productId + " does not exist in cart")
        );
        if (cartEntity.getQuantity() == 1) {
            cartItemRepository.delete(cartEntity);
            return Optional.empty();
        } else {
            cartEntity.decrement();
            return Optional.ofNullable(cartMapper.toResponse(cartEntity));
        }
    }

    @Transactional
    public void deleteProduct(String email, Long productId) {
        UserEntity user = userRepository.findByEmailForCartUpdate(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        Optional<CartEntity> cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productId);
        if (cartEntity.isEmpty()) {
            return;
        }
        cartItemRepository.delete(cartEntity.get());
    }

    @Transactional
    public void clearCart(String email) {
        UserEntity user = userRepository.findByEmailForCartUpdate(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        cartItemRepository.deleteAllByUserId(user.getId());
    }
}

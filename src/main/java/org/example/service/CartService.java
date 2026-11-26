package org.example.service;

import org.example.controller.dto.CartItemsResponse;
import org.example.controller.dto.CartResponse;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.ResourceNotFoundException.Resource;
import org.example.mapper.CartItemsMapper;
import org.example.repository.CartItemRepository;
import org.example.repository.Entities.CartItemEntity;
import org.example.repository.Entities.ProductEntity;
import org.example.repository.Entities.UserEntity;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CartItemsMapper cartItemsMapper;
    private final ProductRepository productRepository;

    public CartService(
        CartItemRepository cartItemRepository,
        UserRepository userRepository,
        CartItemsMapper cartItemsMapper,
        ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.cartItemsMapper = cartItemsMapper;
        this.productRepository = productRepository;
    }

    private CartResponse buildCart(List<CartItemEntity> cartItemEntities) {
        List<CartItemsResponse> responses = cartItemEntities.stream()
            .map(cartItemsMapper::toResponse)
            .toList();
        return CartResponse.builder()
            .totalPrice(responses.stream()
                .map(CartItemsResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .items(responses)
            .build();
    }

    @Transactional(readOnly = true)
    public CartResponse findCart(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist")
        );
        List<CartItemEntity> cartItemEntities = cartItemRepository.findCartEntitiesByUserId(user.getId());
        return buildCart(cartItemEntities);
    }

    @Transactional
    public CartResponse incrementProduct(String email, Long productId) {
        UserEntity user = userRepository.findByEmailForCartUpdate(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        Optional<CartItemEntity> cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(
            user.getId(),
            productId
        );
        if (cartEntity.isEmpty()) {
            ProductEntity product = productRepository.findById(productId).orElseThrow(() ->
                new ResourceNotFoundException(Resource.PRODUCT, productId)
            );
            CartItemEntity entity = CartItemEntity.builder()
                .user(user)
                .product(product)
                .quantity(1)
                .build();
            cartItemRepository.save(entity);
        } else {
            CartItemEntity entity = cartEntity.get();
            entity.increment();
        }
        return buildCart(cartItemRepository.findCartEntitiesByUserId(user.getId()));
    }

    @Transactional
    public CartResponse decrementProduct(String email, Long productId) {
        UserEntity user = userRepository.findByEmailForCartUpdate(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        CartItemEntity cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(
            user.getId(),
            productId
        ).orElseThrow(() ->
            new ResourceNotFoundException("Product with ID: " + productId + " does not exist in cart")
        );
        if (cartEntity.getQuantity() == 1) {
            cartItemRepository.delete(cartEntity);
        } else {
            cartEntity.decrement();
        }
        return buildCart(cartItemRepository.findCartEntitiesByUserId(user.getId()));
    }

    @Transactional
    public CartResponse deleteProduct(String email, Long productId) {
        UserEntity user = userRepository.findByEmailForCartUpdate(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        Optional<CartItemEntity> cartEntity = cartItemRepository.findCartEntitiesByUserIdAndProductId(user.getId(), productId);
        if (cartEntity.isEmpty()) {
            return buildCart(cartItemRepository.findCartEntitiesByUserId(user.getId()));
        }
        cartItemRepository.delete(cartEntity.get());
        return buildCart(cartItemRepository.findCartEntitiesByUserId(user.getId()));
    }

    @Transactional
    public CartResponse clearCart(String email) {
        UserEntity user = userRepository.findByEmailForCartUpdate(email).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        cartItemRepository.deleteAllByUserId(user.getId());
        return buildCart(cartItemRepository.findCartEntitiesByUserId(user.getId()));
    }
}

package org.example.controller;

import org.example.controller.dto.CartResponse;
import org.example.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> findCart(
        @AuthenticationPrincipal
        UserDetails user
    ) {
        CartResponse responses = cartService.findCart(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PostMapping("/items/{productId}/increment")
    public ResponseEntity<CartResponse> incrementProduct(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long productId
    ) {
        CartResponse response = cartService.incrementProduct(user.getUsername(), productId);
        if (response.quantity() == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/items/{productId}/decrement")
    public ResponseEntity<CartResponse> decrementProduct(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long productId
    ) {
        Optional<CartResponse> response = cartService.decrementProduct(user.getUsername(), productId);
        return response.map(cartResponse -> ResponseEntity.status(HttpStatus.OK).body(cartResponse))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(null));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> deleteProduct(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long productId
    ) {
        cartService.deleteProduct(user.getUsername(), productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/items")
    public ResponseEntity<Void> cleanCart(
        @AuthenticationPrincipal UserDetails user
    ) {
        cartService.clearCart(user.getUsername());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

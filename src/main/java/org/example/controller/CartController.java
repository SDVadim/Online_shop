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
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/items/{productId}/decrement")
    public ResponseEntity<CartResponse> decrementProduct(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long productId
    ) {
        CartResponse response = cartService.decrementProduct(user.getUsername(), productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> deleteProduct(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable Long productId
    ) {
        CartResponse cartResponse = cartService.deleteProduct(user.getUsername(), productId);
        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }

    @DeleteMapping("/items")
    public ResponseEntity<CartResponse> cleanCart(
        @AuthenticationPrincipal UserDetails user
    ) {
        CartResponse cartResponse = cartService.clearCart(user.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }
}

package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.AddToCartRequest;
import com.pdnt.restaurant.dto.response.CartResponse;
import com.pdnt.restaurant.entity.Cart;
import com.pdnt.restaurant.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public CartResponse getCart(@PathVariable Long userId) {
        return cartService.getUserCart(userId);
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable Long userId,
            @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addItemToCart(userId, request.getFoodItemId(), request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{userId}/remove/{itemId}")
    public CartResponse removeItem(@PathVariable Long userId, @PathVariable Long itemId) {
        return cartService.removeItemFromCart(userId, itemId);
    }

    @DeleteMapping("/{userId}/clear")
    public CartResponse clearCart(@PathVariable Long userId) {
        return cartService.clearCart(userId);
    }
}


package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.response.CartResponse;
import com.pdnt.restaurant.entity.Cart;
import com.pdnt.restaurant.entity.CartItem;
import com.pdnt.restaurant.entity.FoodItem;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.mapper.CartMapper;
import com.pdnt.restaurant.repository.CartItemRepository;
import com.pdnt.restaurant.repository.CartRepository;
import com.pdnt.restaurant.repository.FoodItemRepository;
import com.pdnt.restaurant.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartResponse getUserCart(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        return cartMapper.toCartResponse(cart);
    }

    public CartResponse addItemToCart(Long userId, Long foodItemId, int quantity) {
        Cart cart = cartRepository.findByUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"))
        ).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(userRepository.findById(userId).get());
            return cartRepository.save(newCart);
        });

        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        CartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getFoodItem().getId().equals(foodItemId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .foodItem(foodItem)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse removeItemFromCart(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"))
        ).orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cartRepository.save(cart);
        return cartMapper.toCartResponse(cart);
    }

    public CartResponse clearCart(Long userId) {
        Cart cart = cartRepository.findByUser(
                userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"))
        ).orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().clear();
        cartRepository.save(cart);
        return cartMapper.toCartResponse(cart);
    }
}


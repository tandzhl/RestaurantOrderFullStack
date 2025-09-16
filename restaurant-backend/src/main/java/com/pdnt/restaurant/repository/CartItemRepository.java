package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}

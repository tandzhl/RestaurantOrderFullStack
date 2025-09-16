package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.Cart;
import com.pdnt.restaurant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}

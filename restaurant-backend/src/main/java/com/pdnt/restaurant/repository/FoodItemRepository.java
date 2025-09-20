package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.dto.response.FoodItemResponse;
import com.pdnt.restaurant.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findAllByMenuIdAndActiveTrue(Long menuId);

    Page<FoodItem> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    Page<FoodItem> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Page<FoodItem> findAllByActiveTrue(Pageable pageable);

    @Query("SELECT f FROM FoodItem f WHERE f.menu.restaurant.id = :restaurantId AND f.active = true")
    List<FoodItem> findByRestaurantIdAndActiveTrue(@Param("restaurantId") Long restaurantId);

    Optional<FoodItem> findByIdAndActiveTrue(Long id); // ✅ dùng cho get chi tiết
}

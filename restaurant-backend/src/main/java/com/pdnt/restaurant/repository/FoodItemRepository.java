package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.dto.response.FoodItemResponse;
import com.pdnt.restaurant.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findAllByMenuId(Long menuId);
    Page<FoodItem> findByCategoryId(Long categoryId, Pageable pageable);
    Page<FoodItem> findByNameContainingIgnoreCase(String name, Pageable pageable);
    @Query("SELECT f FROM FoodItem f WHERE f.menu.restaurant.id = :restaurantId")
    List<FoodItem> findByRestaurantId(@Param("restaurantId") Long restaurantId);
}

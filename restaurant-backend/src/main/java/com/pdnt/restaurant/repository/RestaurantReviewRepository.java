package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.RestaurantReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantReviewRepository extends JpaRepository<RestaurantReview, Long> {
    @Query("SELECT AVG(fr.rating) FROM RestaurantReview fr WHERE fr.restaurant.id = :restaurantId")
    Double findAverageRatingByRestaurant(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COUNT(r) FROM RestaurantReview r WHERE r.restaurant.id = :restaurantId")
    long countByRestaurantId(@Param("restaurantId") Long restaurantId);

    List<RestaurantReview> findByRestaurantId(Long restaurantId);
}

package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.FoodReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodReviewRepository extends JpaRepository<FoodReview, Long> {
    @Query("SELECT AVG(fr.rating) FROM FoodReview fr WHERE fr.foodItem.id = :foodItemId")
    Double findAverageRatingByFoodItemId(@Param("foodItemId") Long foodItemId);

    List<FoodReview> findByFoodItemId(Long foodItemId);
}

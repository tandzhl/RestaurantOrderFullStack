package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.Follow;
import com.pdnt.restaurant.entity.Restaurant;
import com.pdnt.restaurant.entity.composite_keys.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    boolean existsById(FollowId id);
    void deleteById(FollowId id);
    List<Follow> findByCustomer_Id(Long customerId);
    List<Follow> findByRestaurantId(Long restaurantId);
}

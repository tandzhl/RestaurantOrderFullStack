package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.Restaurant;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.entity.enums.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByOwner(User owner);
    List<Restaurant> findByOwnerAndStatus(User owner, RestaurantStatus status);

}

package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.response.RestaurantResponse;
import com.pdnt.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/restaurants")
@RequiredArgsConstructor
public class AdminRestaurantController {
    private final RestaurantService restaurantService;

    @PutMapping("/{id}/approve")
    public ResponseEntity<RestaurantResponse> approveRestaurant(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.approveRestaurant(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<RestaurantResponse> rejectRestaurant(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.rejectRestaurant(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")// chỉ admin
    public ResponseEntity<List<RestaurantResponse>> getPendingRestaurants() {
        return ResponseEntity.ok(restaurantService.getPendingRestaurants());
    }
}
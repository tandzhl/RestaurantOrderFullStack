package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.RestaurantRequest;
import com.pdnt.restaurant.dto.response.MenuResponse;
import com.pdnt.restaurant.dto.response.RestaurantResponse;
import com.pdnt.restaurant.entity.Menu;
import com.pdnt.restaurant.entity.Restaurant;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.service.MenuService;
import com.pdnt.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final MenuService menuService;

    @GetMapping
    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    // Lấy chi tiết một nhà hàng
    @GetMapping("/{id}")
    public RestaurantResponse getRestaurantById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    @GetMapping("/{id}/menus")
    public ResponseEntity<List<MenuResponse>> getMenusByRestaurant(@PathVariable("id") Long restaurantId) {
        List<MenuResponse> menus = menuService.getMenusByRestaurant(restaurantId);
        return ResponseEntity.ok(menus);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestaurantResponse> registerRestaurant(
            @ModelAttribute RestaurantRequest request
    ) {
        RestaurantResponse response = restaurantService.registerRestaurant(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-restaurant")
    public ResponseEntity<List<RestaurantResponse>> getMyRestaurants() {
        return ResponseEntity.ok(restaurantService.getMyApprovedRestaurants());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @ModelAttribute RestaurantRequest request,
            @AuthenticationPrincipal User user
    ) {
        Long currentUserId = user.getId();
        RestaurantResponse response = restaurantService.updateRestaurant(id, request, currentUserId);
        return ResponseEntity.ok(response);
    }
}

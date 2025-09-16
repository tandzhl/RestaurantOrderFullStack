package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.RestaurantReviewRequest;
import com.pdnt.restaurant.dto.request.RestaurantReviewUpdateRequest;
import com.pdnt.restaurant.dto.response.RestaurantReviewResponse;
import com.pdnt.restaurant.entity.RestaurantReview;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.mapper.RestaurantReviewMapper;
import com.pdnt.restaurant.service.RestaurantReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurant-reviews")
@RequiredArgsConstructor
public class RestaurantReviewController {
    private final RestaurantReviewService restaurantReviewService;
    private final RestaurantReviewMapper restaurantReviewMapper;

    @GetMapping("/{id}")
    public ResponseEntity<List<RestaurantReviewResponse>> getByRestaurant(@PathVariable Long id) {
        List<RestaurantReviewResponse> responses = restaurantReviewService.getRestaurantReview(id)
                .stream()
                .map(restaurantReviewMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<RestaurantReviewResponse> create(@ModelAttribute RestaurantReviewRequest request,
                                                     @AuthenticationPrincipal User currentUser)     {
        RestaurantReview review = restaurantReviewService.create(request, currentUser);
        return ResponseEntity.ok(restaurantReviewMapper.toResponse(review));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantReviewResponse> update(@PathVariable Long id,
                                                     @ModelAttribute RestaurantReviewUpdateRequest request,
                                                     @AuthenticationPrincipal User currentUser) {
        RestaurantReview review = restaurantReviewService.update(id, request, currentUser);
        return ResponseEntity.ok(restaurantReviewMapper.toResponse(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        restaurantReviewService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}

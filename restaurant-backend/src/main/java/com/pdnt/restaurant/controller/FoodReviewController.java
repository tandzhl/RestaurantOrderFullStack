package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.FoodReviewRequest;
import com.pdnt.restaurant.dto.request.FoodReviewUpdateRequest;
import com.pdnt.restaurant.dto.response.FoodReviewResponse;
import com.pdnt.restaurant.entity.FoodReview;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.mapper.FoodReviewMapper;
import com.pdnt.restaurant.service.FoodReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/food-reviews")
@RequiredArgsConstructor
public class FoodReviewController {
    private final FoodReviewService foodReviewService;
    private final FoodReviewMapper foodReviewMapper;

    @GetMapping("/{id}")
    public ResponseEntity<List<FoodReviewResponse>> getByFoodItem(@PathVariable Long id) {
        List<FoodReviewResponse> responses = foodReviewService.getByFoodItem(id)
                .stream()
                .map(foodReviewMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<FoodReviewResponse> create(@ModelAttribute FoodReviewRequest request,
                                                     @AuthenticationPrincipal User currentUser)     {
        FoodReview review = foodReviewService.create(request, currentUser);
        return ResponseEntity.ok(foodReviewMapper.toResponse(review));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodReviewResponse> update(@PathVariable Long id,
                                                     @ModelAttribute FoodReviewUpdateRequest request,
                                                     @AuthenticationPrincipal User currentUser) {
        FoodReview review = foodReviewService.update(id, request, currentUser);
        return ResponseEntity.ok(foodReviewMapper.toResponse(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        foodReviewService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}

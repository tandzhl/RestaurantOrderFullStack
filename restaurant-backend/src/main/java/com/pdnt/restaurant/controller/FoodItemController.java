package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.FoodItemForm;
import com.pdnt.restaurant.dto.response.FoodItemResponse;
import com.pdnt.restaurant.entity.FoodItem;
import com.pdnt.restaurant.service.FoodItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/food-items")
public class FoodItemController {
    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    @PostMapping
    public ResponseEntity<FoodItemResponse> create(@ModelAttribute FoodItemForm form) throws IOException {
        return ResponseEntity.ok(foodItemService.createFoodItem(form));
    }
    @GetMapping("/{id}")
    public FoodItemResponse getFoodItemById(@PathVariable Long id) {
        return foodItemService.getFoodItemById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodItemResponse> update(@PathVariable Long id,
                                           @ModelAttribute FoodItemForm form) throws IOException {
        return ResponseEntity.ok(foodItemService.updateFoodItem(id, form));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/menu/{menuId}")
    public ResponseEntity<List<FoodItemResponse>> getAllByMenu(@PathVariable Long menuId) {
        return ResponseEntity.ok(foodItemService.getAllByMenu(menuId));
    }

    @GetMapping("/category/{categoryId}")
    public Page<FoodItemResponse> getFoodItemsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page // mặc định trang 0
    ) {
        return foodItemService.getFoodItemsByCategory(categoryId, page);
    }

    @GetMapping
    public Page<FoodItemResponse> getAllFoodItems(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size // ✅ mặc định 12
    ) {
        return foodItemService.getAllFoodItems(name, page, size);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<FoodItemResponse>> getFoodItemsByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(foodItemService.getFoodItemsByRestaurant(restaurantId));
    }
}

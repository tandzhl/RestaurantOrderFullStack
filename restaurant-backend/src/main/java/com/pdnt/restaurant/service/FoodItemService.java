package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.request.CreateNotificationRequest;
import com.pdnt.restaurant.dto.request.FoodItemForm;
import com.pdnt.restaurant.dto.response.FoodCreateNotificationResponse;
import com.pdnt.restaurant.dto.response.FoodItemResponse;
import com.pdnt.restaurant.entity.Category;
import com.pdnt.restaurant.entity.FoodItem;
import com.pdnt.restaurant.entity.Menu;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.exceptions.ErrorCode;
import com.pdnt.restaurant.exceptions.WebException;
import com.pdnt.restaurant.mapper.FoodItemMapper;
import com.pdnt.restaurant.repository.CategoryRepository;
import com.pdnt.restaurant.repository.FoodItemRepository;
import com.pdnt.restaurant.repository.FoodReviewRepository;
import com.pdnt.restaurant.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodItemService {
    private final FoodItemRepository foodItemRepository;
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final FoodItemMapper foodItemMapper;
    private final CloudinaryService cloudinaryService;
    private final FoodReviewRepository foodReviewRepository;
    private final NotificationService notificationService; // ✅ thêm service
    private final RestTemplate restTemplate = new RestTemplate();

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if(principal instanceof User user) {
            return user;
        }
        throw new WebException(ErrorCode.UNAUTHORIZED);
    }

    public void checkOwnerPermission(Menu menu) {
        User currentUser = getCurrentUser();
        if(!menu.getRestaurant().getOwner().getId().equals(currentUser.getId())) {
            throw new WebException(ErrorCode.FORBIDDEN);
        }
    }

    public FoodItemResponse createFoodItem(FoodItemForm form) throws IOException {
        FoodItem foodItem = foodItemMapper.toEntity(form);
        foodItem.setActive(true);
        // upload ảnh lên cloudinary
        if (form.getImage() != null && !form.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(form.getImage());
            foodItem.setImageUrl(imageUrl);
        }

        Menu menu = menuRepository.findById(form.getMenuId())
                .orElseThrow(() -> new RuntimeException("Menu not found"));
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        checkOwnerPermission(menu);

        foodItem.setMenu(menu);
        foodItem.setCategory(category);

        FoodItem saved = foodItemRepository.save(foodItem);
        Double avgRating = foodReviewRepository.findAverageRatingByFoodItemId(saved.getId());

        List<FoodCreateNotificationResponse> notifications = notifyFollowers(menu.getRestaurant().getId(), saved);
        return foodItemMapper.toDto(saved, avgRating != null ? avgRating : 0.0);
    }

    public FoodItemResponse updateFoodItem(Long id, FoodItemForm form) throws IOException {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FoodItem not found"));

        checkOwnerPermission(foodItem.getMenu());

        foodItemMapper.updateFoodItemFromForm(form, foodItem);

        if (form.getMenuId() != null) {
            foodItem.setMenu(menuRepository.findById(form.getMenuId())
                    .orElseThrow(() -> new RuntimeException("Menu not found")));
        }

        if (form.getCategoryId() != null) {
            foodItem.setCategory(categoryRepository.findById(form.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found")));
        }

        if (form.getImage() != null && !form.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(form.getImage());
            foodItem.setImageUrl(imageUrl);
        }

        FoodItem saved = foodItemRepository.save(foodItem);
        Double avgRating = foodReviewRepository.findAverageRatingByFoodItemId(saved.getId());

        return foodItemMapper.toDto(saved, avgRating != null ? avgRating : 0.0);
    }

    public List<FoodItemResponse> getAllByMenu(Long menuId) {
        return foodItemRepository.findAllByMenuIdAndActiveTrue(menuId)
                .stream()
                .map(foodItem -> {
                    Double avgRating = foodReviewRepository.findAverageRatingByFoodItemId(foodItem.getId());
                    return foodItemMapper.toDto(foodItem, avgRating != null ? avgRating : 0.0);
                })
                .collect(Collectors.toList());
    }

    public void deleteFoodItem(Long id) {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FoodItem not found"));

        checkOwnerPermission(foodItem.getMenu());

        foodItem.setActive(false); // ✅ chỉ đổi trạng thái
        foodItemRepository.save(foodItem);
    }

    public Page<FoodItemResponse> getFoodItemsByCategory(Long categoryId, int page) {
        PageRequest pageable = PageRequest.of(page, 12); // 12 món / trang
        return foodItemRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(foodItem -> {
                    Double avgRating = foodReviewRepository.findAverageRatingByFoodItemId(foodItem.getId());
                    return foodItemMapper.toDto(foodItem, avgRating != null ? avgRating : 0.0);
                });
    }

    public FoodItemResponse getFoodItemById(Long id) {
        FoodItem foodItem = foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found Food item not found"));
        Double avgRating = foodReviewRepository.findAverageRatingByFoodItemId(foodItem.getId());
        return foodItemMapper.toDto(foodItem, avgRating != null ? avgRating : 0.0);
    }

    public Page<FoodItemResponse> getAllFoodItems(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<FoodItem> foodPage;
        if (name != null && !name.isBlank()) {
            foodPage = foodItemRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable);
        } else {
            foodPage = foodItemRepository.findAllByActiveTrue(pageable);
        }

        return foodPage.map(food -> {
            Double avgRating = foodReviewRepository.findAverageRatingByFoodItemId(food.getId());
            return foodItemMapper.toDto(food, avgRating != null ? avgRating : 0.0);
        });
    }

    public List<FoodItemResponse> getFoodItemsByRestaurant(Long restaurantId) {
        List<FoodItem> foodItems = foodItemRepository.findByRestaurantIdAndActiveTrue(restaurantId);
        return foodItemMapper.toResponseList(foodItems);
    }

    private List<FoodCreateNotificationResponse> notifyFollowers(Long restaurantId, FoodItem foodItem) {
        String url = "http://localhost:8080/follow/restaurant/" + restaurantId + "/followers";
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

        if (response.getBody() == null || response.getBody().isEmpty()) return List.of();

        List<Long> userIds = ((List<Map<String, Object>>) response.getBody()).stream()
                .map(f -> Long.valueOf(f.get("customerId").toString()))
                .toList();

        return notificationService.createFoodNotifications(
                userIds,
                "Món mới tại " + foodItem.getMenu().getRestaurant().getName(),
                "Nhà hàng vừa thêm món: " + foodItem.getName(),
                foodItem.getId()
        );
    }
}

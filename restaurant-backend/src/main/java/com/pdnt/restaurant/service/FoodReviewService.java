package com.pdnt.restaurant.service;

import com.cloudinary.Cloudinary;
import com.pdnt.restaurant.dto.request.FoodReviewRequest;
import com.pdnt.restaurant.dto.request.FoodReviewUpdateRequest;
import com.pdnt.restaurant.entity.FoodItem;
import com.pdnt.restaurant.entity.FoodReview;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.exceptions.ErrorCode;
import com.pdnt.restaurant.exceptions.WebException;
import com.pdnt.restaurant.mapper.FoodReviewMapper;
import com.pdnt.restaurant.repository.FoodItemRepository;
import com.pdnt.restaurant.repository.FoodReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodReviewService {
    private final FoodReviewRepository foodReviewRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodReviewMapper foodReviewMapper;
    private final CloudinaryService cloudinaryService;

    public List<FoodReview> getByFoodItem(Long foodItemId) {
        return foodReviewRepository.findByFoodItemId(foodItemId);
    }

    public FoodReview create(FoodReviewRequest request, User user) {
        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));

        // Dùng MapStruct thay vì builder()
        FoodReview review = foodReviewMapper.toEntity(request, user, foodItem);

        if (request.getImage() != null && !request.getImage().isEmpty()) { // ✅ ảnh optional
            try {
                String imageUrl = cloudinaryService.uploadFile(request.getImage());
                review.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new WebException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        return foodReviewRepository.save(review);
    }

    public FoodReview update(Long id, FoodReviewUpdateRequest request, User user) {
        FoodReview review = foodReviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        // Chỉ chủ review mới được sửa
        if (!review.getUser().getId().equals(user.getId())) {
            throw new WebException(ErrorCode.REVIEW_UPDATE_FORBIDDEN);
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(request.getImage());
                review.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed");
            }
        }

        return foodReviewRepository.save(review);
    }

    public void delete(Long id, User user) {
        FoodReview review = foodReviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        // Chỉ chủ review mới được xóa
        if (!review.getUser().getId().equals(user.getId())) {
            throw new WebException(ErrorCode.REVIEW_DELETE_FORBIDDEN);
        }

        foodReviewRepository.delete(review);
    }
}
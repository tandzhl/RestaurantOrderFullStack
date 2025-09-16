package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.request.RestaurantReviewRequest;
import com.pdnt.restaurant.dto.request.RestaurantReviewUpdateRequest;
import com.pdnt.restaurant.entity.*;
import com.pdnt.restaurant.exceptions.ErrorCode;
import com.pdnt.restaurant.exceptions.WebException;
import com.pdnt.restaurant.mapper.RestaurantReviewMapper;
import com.pdnt.restaurant.repository.RestaurantRepository;
import com.pdnt.restaurant.repository.RestaurantReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantReviewService {
    private final RestaurantReviewRepository restaurantReviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantReviewMapper restaurantReviewMapper;
    private final CloudinaryService cloudinaryService;

    public List<RestaurantReview> getRestaurantReview(Long restaurantId) {
        return restaurantReviewRepository.findByRestaurantId(restaurantId);
    }

    public RestaurantReview create(RestaurantReviewRequest request, User user) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant item not found"));

        RestaurantReview review = restaurantReviewMapper.toEntity(request, user, restaurant);

        if (request.getImage() != null && !request.getImage().isEmpty()) { // ✅ ảnh optional
            try {
                String imageUrl = cloudinaryService.uploadFile(request.getImage());
                review.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new WebException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        return restaurantReviewRepository.save(review);
    }

    public RestaurantReview update(Long id, RestaurantReviewUpdateRequest request, User user) {
        RestaurantReview review = restaurantReviewRepository.findById(id)
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

        return restaurantReviewRepository.save(review);
    }

    public void delete(Long id, User user) {
        RestaurantReview review = restaurantReviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        // Chỉ chủ review mới được xóa
        if (!review.getUser().getId().equals(user.getId())) {
            throw new WebException(ErrorCode.REVIEW_DELETE_FORBIDDEN);
        }

        restaurantReviewRepository.delete(review);
    }
}

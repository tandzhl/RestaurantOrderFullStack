package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.request.RestaurantRequest;
import com.pdnt.restaurant.dto.response.RestaurantResponse;
import com.pdnt.restaurant.entity.Restaurant;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.entity.enums.RestaurantStatus;
import com.pdnt.restaurant.entity.enums.Role;
import com.pdnt.restaurant.exceptions.ErrorCode;
import com.pdnt.restaurant.exceptions.WebException;
import com.pdnt.restaurant.mapper.RestaurantMapper;
import com.pdnt.restaurant.repository.RestaurantRepository;
import com.pdnt.restaurant.repository.RestaurantReviewRepository;
import com.pdnt.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantReviewRepository restaurantReviewRepository;
    private final RestaurantMapper restaurantMapper;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

    public List<RestaurantResponse> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByStatus(RestaurantStatus.APPROVED);
        return restaurants.stream().map(restaurant -> {
            RestaurantResponse response = restaurantMapper.toResponse(restaurant);

            Double avgRating = restaurantReviewRepository.findAverageRatingByRestaurant(restaurant.getId());
            response.setAverageRating(avgRating != null ? avgRating : 0.0);


            Long totalReviews = restaurantReviewRepository.countByRestaurantId(restaurant.getId());
            response.setTotalReviews(totalReviews);

            return response;
        }).toList();
    }

    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        if (restaurant.getStatus() != RestaurantStatus.APPROVED) {
            throw new RuntimeException("Restaurant not approved yet");
        }

        RestaurantResponse response = restaurantMapper.toResponse(restaurant);
        Double avgRating = restaurantReviewRepository.findAverageRatingByRestaurant(id);
        response.setAverageRating(avgRating != null ? avgRating : 0.0);

        Long totalReviews = restaurantReviewRepository.countByRestaurantId(id);
        response.setTotalReviews(totalReviews);
        return response;
    }

    public RestaurantResponse registerRestaurant(RestaurantRequest request) {
        // Lấy current user từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Upload ảnh lên Cloudinary
        String imageUrl = null;
        try {
            if (request.getImage() != null && !request.getImage().isEmpty()) {
                imageUrl = cloudinaryService.uploadFile(request.getImage());
            }
        } catch (IOException e) {
            throw new RuntimeException("Upload image failed", e);
        }

        // Tạo entity Restaurant
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .imageUrl(imageUrl)
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .owner(owner)
                .status(RestaurantStatus.PENDING) // mặc định pending để admin duyệt
                .build();

        restaurantRepository.save(restaurant);

        return restaurantMapper.toResponse(restaurant);
    }

    public RestaurantResponse approveRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setStatus(RestaurantStatus.APPROVED);

        // cập nhật role cho owner
        User owner = restaurant.getOwner();
        owner.setRole(Role.RESTAURANT_OWNER); // giả sử bạn có enum Role
        userRepository.save(owner);

        restaurantRepository.save(restaurant);

        return restaurantMapper.toResponse(restaurant);
    }

    public RestaurantResponse rejectRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        restaurant.setStatus(RestaurantStatus.REJECTED);
        restaurantRepository.save(restaurant);

        return restaurantMapper.toResponse(restaurant);
    }

    public List<RestaurantResponse> getPendingRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByStatus(RestaurantStatus.PENDING);
        return restaurantMapper.toResponseList(restaurants);
    }

    public List<RestaurantResponse> getMyApprovedRestaurants() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Restaurant> restaurants = restaurantRepository.findByOwnerAndStatus(owner, RestaurantStatus.APPROVED);

        return restaurants.stream().map(restaurant -> {
            RestaurantResponse response = restaurantMapper.toResponse(restaurant);

            Double avgRating = restaurantReviewRepository.findAverageRatingByRestaurant(restaurant.getId());
            response.setAverageRating(avgRating != null ? avgRating : 0.0);

            Long totalReviews = restaurantReviewRepository.countByRestaurantId(restaurant.getId());
            response.setTotalReviews(totalReviews);

            return response;
        }).toList();
    }

    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request, Long currentUserId) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // ✅ Chỉ owner mới được sửa
        if (!restaurant.getOwner().getId().equals(currentUserId)) {
            throw new WebException(ErrorCode.FORBIDDEN);
        }

        // chỉ update khi có dữ liệu
        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }
        if (request.getOpeningTime() != null) {
            restaurant.setOpeningTime(request.getOpeningTime());
        }
        if (request.getClosingTime() != null) {
            restaurant.setClosingTime(request.getClosingTime());
        }

        MultipartFile image = request.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadFile(image);
                restaurant.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi upload ảnh", e);
            }
        }

        Restaurant saved = restaurantRepository.save(restaurant);
        return restaurantMapper.toResponse(saved);
    }

}

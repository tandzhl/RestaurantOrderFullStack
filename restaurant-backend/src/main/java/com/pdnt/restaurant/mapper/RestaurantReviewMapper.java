package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.request.FoodReviewRequest;
import com.pdnt.restaurant.dto.request.RestaurantReviewRequest;
import com.pdnt.restaurant.dto.response.FoodReviewResponse;
import com.pdnt.restaurant.dto.response.RestaurantReviewResponse;
import com.pdnt.restaurant.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestaurantReviewMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "restaurant", source = "restaurant")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "rating", source = "request.rating")
    @Mapping(target = "comment", source = "request.comment")
    RestaurantReview toEntity(RestaurantReviewRequest request, User user, Restaurant restaurant);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "imgUrl", source = "imageUrl")
    @Mapping(target = "userFullname", expression = "java(review.getUser().getFirstName() + \" \" + review.getUser().getLastName())")
    @Mapping(target = "restaurantId", source = "restaurant.id")
    RestaurantReviewResponse toResponse(RestaurantReview review);
}

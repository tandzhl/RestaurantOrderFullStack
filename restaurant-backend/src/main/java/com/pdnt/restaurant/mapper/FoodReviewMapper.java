package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.request.FoodReviewRequest;
import com.pdnt.restaurant.dto.response.FoodReviewResponse;
import com.pdnt.restaurant.entity.FoodItem;
import com.pdnt.restaurant.entity.FoodReview;
import com.pdnt.restaurant.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FoodReviewMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "foodItem", source = "foodItem")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "rating", source = "request.rating")
    @Mapping(target = "comment", source = "request.comment")
    FoodReview toEntity(FoodReviewRequest request, User user, FoodItem foodItem);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "imgUrl", source = "imageUrl")
    @Mapping(target = "userFullname", expression = "java(review.getUser().getFirstName() + \" \" + review.getUser().getLastName())")
    @Mapping(target = "foodItemId", source = "foodItem.id")
    FoodReviewResponse toResponse(FoodReview review);
}

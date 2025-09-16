package com.pdnt.restaurant.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class RestaurantReviewResponse {
    private Long id;
    private Double rating;
    private String comment;
    private LocalDateTime createAt;
    private String imgUrl;
    private Long userId;
    private String username;
    private String userFullname;
    private Long restaurantId;
}

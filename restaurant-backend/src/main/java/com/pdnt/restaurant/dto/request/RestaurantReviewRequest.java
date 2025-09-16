package com.pdnt.restaurant.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class RestaurantReviewRequest {
    private Double rating;
    private String comment;
    private Long restaurantId;
    private MultipartFile image;
}


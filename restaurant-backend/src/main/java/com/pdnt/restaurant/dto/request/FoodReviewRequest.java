package com.pdnt.restaurant.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FoodReviewRequest {
    private Double rating;
    private String comment;
    private Long foodItemId;
    private MultipartFile image;
}

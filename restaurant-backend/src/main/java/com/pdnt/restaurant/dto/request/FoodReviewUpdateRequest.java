package com.pdnt.restaurant.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FoodReviewUpdateRequest {
    private Double rating;
    private String comment;
    private MultipartFile image; // ảnh mới (optional)
}
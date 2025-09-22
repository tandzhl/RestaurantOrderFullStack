package com.pdnt.restaurant.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FoodItemForm {
    private String name;
    private Integer price;
    private String description;
    private Long menuId;
    private Long categoryId;
    private MultipartFile image; // nhận file upload
    private boolean active;
}


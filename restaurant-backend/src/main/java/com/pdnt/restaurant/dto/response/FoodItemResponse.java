package com.pdnt.restaurant.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItemResponse {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String imageUrl;
    private Long menuId;
    private Long categoryId;
    private Long restaurantId;
    private Double averageRating;
}
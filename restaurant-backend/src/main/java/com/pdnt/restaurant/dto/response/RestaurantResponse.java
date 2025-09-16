package com.pdnt.restaurant.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private Long id;          // ID nhà hàng
    private String name;      // Tên nhà hàng
    private String address; // Trạng thái
    private Long ownerId;     // ID chủ nhà hàng
    private String imageUrl;  // Ảnh
    private Double averageRating;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Long totalReviews;
}



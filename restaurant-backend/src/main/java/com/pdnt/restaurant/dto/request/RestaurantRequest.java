package com.pdnt.restaurant.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class RestaurantRequest {
    private String name;
    private String address;
    private MultipartFile image;
    private LocalTime openingTime;
    private LocalTime closingTime;
}

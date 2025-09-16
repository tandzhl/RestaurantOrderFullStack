package com.pdnt.restaurant.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FoodCreateNotificationResponse {
    private Long foodItemId;
    private Long id;
    private Long recipientId; // map từ user.id
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;
}

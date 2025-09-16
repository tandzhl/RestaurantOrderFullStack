package com.pdnt.restaurant.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFoodNotificationRequest {
    private List<Long> userIds;
    private String title;
    private String message;
    private String type; // NEW_FOOD
    private Long foodId;
}
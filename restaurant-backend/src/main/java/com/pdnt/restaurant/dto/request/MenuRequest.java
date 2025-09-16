package com.pdnt.restaurant.dto.request;

import lombok.Data;
@Data
public class MenuRequest {
    private String name;
    private Long restaurantId;
}
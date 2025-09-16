package com.pdnt.restaurant.dto.request;

import lombok.Data;
@Data
public class AddToCartRequest {
    private Long foodItemId; // ID món ăn
    private int quantity;    // Số lượng
}

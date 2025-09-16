package com.pdnt.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long foodId;       // ID món ăn
    private String foodName;   // Tên món ăn
    private Double price;      // Giá
    private int quantity;      // Số lượng
    private Double subtotal;   // Thành tiền
}

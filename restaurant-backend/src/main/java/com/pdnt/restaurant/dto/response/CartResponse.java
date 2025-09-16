package com.pdnt.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long cartId;          // ID giỏ hàng
    private Long userId;          // ID người dùng
    private Double total;         // Tổng tiền
    private List<CartItemResponse> items; // Danh sách món
}

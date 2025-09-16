package com.pdnt.restaurant.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    private Long customerId;
    private String payment; // CASH, CARD...
    private List<CartItemDTO> items;

    @Data
    public static class CartItemDTO {
        private Long foodItemId;
        private Long restaurantId;
        private int quantity;
    }
}

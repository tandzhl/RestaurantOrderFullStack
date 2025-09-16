package com.pdnt.restaurant.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Long foodItemId;
    private String foodName;
    private Integer quantity;
    private Double price;
}

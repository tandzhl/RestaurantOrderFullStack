package com.pdnt.restaurant.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Double totalAmount;
    private String status;
    private String payment;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
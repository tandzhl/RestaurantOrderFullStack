package com.pdnt.restaurant.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {
    @NotNull
    private Long restaurantId;

    @NotEmpty
    private List<OrderItemRequest> items;

    // optional: "VNPAY" or "CASH" — service sẽ parse nếu cần
    private String payment;
}
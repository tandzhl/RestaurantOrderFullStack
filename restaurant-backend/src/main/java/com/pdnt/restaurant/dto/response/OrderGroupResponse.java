package com.pdnt.restaurant.dto.response;

import com.pdnt.restaurant.entity.enums.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderGroupResponse {
    private Long id;
    private LocalDateTime createdAt;
    private Payment payment;
    private String status;
    private Double totalAmount;
    private Long customerId;
}

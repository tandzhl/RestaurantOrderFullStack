package com.pdnt.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueMonthResponse {
    private int month;
    private int year;
    private long successOrders;
    private double totalRevenue;
}
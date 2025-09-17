package com.pdnt.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueYearResponse {
    private int year;
    private long successOrder;
    private double totalRevenue;
}

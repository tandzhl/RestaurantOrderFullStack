package com.pdnt.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueWeekResponse {
    private int week;
    private LocalDate startDate;
    private LocalDate endDate;
    private long successOrders;
    private double totalRevenue;
}

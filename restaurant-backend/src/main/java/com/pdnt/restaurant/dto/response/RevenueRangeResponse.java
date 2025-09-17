package com.pdnt.restaurant.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevenueRangeResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private long successOrders;   // số đơn thành công
    private double totalRevenue;  // tổng doanh thu
}

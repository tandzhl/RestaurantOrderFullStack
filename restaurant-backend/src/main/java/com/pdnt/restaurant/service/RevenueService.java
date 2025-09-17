package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.response.RevenueMonthResponse;
import com.pdnt.restaurant.dto.response.RevenueRangeResponse;
import com.pdnt.restaurant.dto.response.RevenueWeekResponse;
import com.pdnt.restaurant.dto.response.RevenueYearResponse;
import com.pdnt.restaurant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueService {
    private final OrderRepository orderRepository;

    public List<RevenueMonthResponse> getRevenueByMonth(Long restaurantId) {
        List<Object[]> results = orderRepository.getRevenueByMonth(restaurantId);
        return results.stream()
                .map(r -> new RevenueMonthResponse(
                        (int) r[0],          // month
                        (int) r[1],          // year
                        (long) r[2],         // số đơn
                        ((Number) r[3]).doubleValue() // doanh thu
                ))
                .toList();
    }

    public List<RevenueYearResponse> getRevenueByYear(Long restaurantId) {
        List<Object[]> results = orderRepository.getRevenueByYear(restaurantId);
        return results.stream()
                .map(r -> new RevenueYearResponse(
                        (int) r[0],          // year
                        (long) r[1],         // số đơn
                        ((Number) r[2]).doubleValue() // doanh thu
                ))
                .toList();
    }

    public RevenueRangeResponse getRevenueByDateRange(Long restaurantId, LocalDate start, LocalDate end) {
        List<Object[]> result = orderRepository.getRevenueByDateRange(
                restaurantId,
                start.atStartOfDay(),
                end.atTime(LocalTime.MAX)
        );

        Object[] data = result.isEmpty() ? new Object[]{0L, 0.0} : result.get(0);

        long count = data[0] != null ? ((Number) data[0]).longValue() : 0L;
        double revenue = data[1] != null ? ((Number) data[1]).doubleValue() : 0.0;

        return RevenueRangeResponse.builder()
                .startDate(start)
                .endDate(end)
                .successOrders(count)
                .totalRevenue(revenue)
                .build();
    }
    public List<RevenueWeekResponse> getRevenueByWeeks(Long restaurantId, int year, int month) {
        List<RevenueWeekResponse> result = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        LocalDate currentStart = firstDay;
        int week = 1;

        while (!currentStart.isAfter(lastDay)) {
            LocalDate currentEnd = currentStart.plusDays(6);
            if (currentEnd.isAfter(lastDay)) {
                currentEnd = lastDay;
            }

            List<Object[]> queryResult = orderRepository.getRevenueByDateRange(
                    restaurantId,
                    currentStart.atStartOfDay(),
                    currentEnd.atTime(LocalTime.MAX)
            );

            Object[] data = queryResult.isEmpty() ? new Object[]{0L, 0.0} : queryResult.get(0);

            long count = data[0] != null ? ((Number) data[0]).longValue() : 0L;
            double revenue = data[1] != null ? ((Number) data[1]).doubleValue() : 0.0;

            result.add(RevenueWeekResponse.builder()
                    .week(week)
                    .startDate(currentStart)
                    .endDate(currentEnd)
                    .successOrders(count)
                    .totalRevenue(revenue)
                    .build());

            currentStart = currentEnd.plusDays(1);
            week++;
        }

        return result;
    }
}

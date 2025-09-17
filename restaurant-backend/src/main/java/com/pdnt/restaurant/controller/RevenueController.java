package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.response.RevenueMonthResponse;
import com.pdnt.restaurant.dto.response.RevenueRangeResponse;
import com.pdnt.restaurant.dto.response.RevenueWeekResponse;
import com.pdnt.restaurant.dto.response.RevenueYearResponse;
import com.pdnt.restaurant.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class RevenueController {
    private final RevenueService revenueService;

    @GetMapping("/revenue/month/{restaurantId}")
    public ResponseEntity<List<RevenueMonthResponse>> getRevenueByMonth(
            @PathVariable Long restaurantId
    ) {
        return ResponseEntity.ok(revenueService.getRevenueByMonth(restaurantId));
    }

    @GetMapping("/revenue/year/{restaurantId}")
    public ResponseEntity<List<RevenueYearResponse>> getRevenueByYear(
            @PathVariable Long restaurantId
    ) {
        return ResponseEntity.ok(revenueService.getRevenueByYear(restaurantId));
    }

    @GetMapping("/revenue/range/{restaurantId}")
    public ResponseEntity<RevenueRangeResponse> getRevenueByDateRange(
            @PathVariable Long restaurantId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        return ResponseEntity.ok(revenueService.getRevenueByDateRange(restaurantId, start, end));
    }

    @GetMapping("/revenue/weeks/{restaurantId}")
    public ResponseEntity<List<RevenueWeekResponse>> getRevenueByWeeks(
            @PathVariable Long restaurantId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(revenueService.getRevenueByWeeks(restaurantId, year, month));
    }
}

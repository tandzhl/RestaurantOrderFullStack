package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.OrderCreateRequest;
import com.pdnt.restaurant.dto.response.OrderResponse;
import com.pdnt.restaurant.entity.Order;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.mapper.OrderMapper;
import com.pdnt.restaurant.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Page<OrderResponse>> getOrdersByRestaurant(
            @PathVariable Long restaurantId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant(restaurantId, pageable));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User currentUser) {
        try {
            orderService.cancelOrder(orderId, currentUser.getId());
            return ResponseEntity.ok("Đã hủy đơn thành công!");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền hủy đơn này");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


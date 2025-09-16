package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.response.OrderGroupResponse;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-groups")
@RequiredArgsConstructor
public class OrderGroupController {

    private final CheckoutService orderGroupService;

    @GetMapping
    public ResponseEntity<List<OrderGroupResponse>> getMyOrderGroups(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(orderGroupService.getOrderGroupsByUser(currentUser.getId()));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderGroupResponse> payOrderGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(orderGroupService.payOrderGroup(id, currentUser));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderGroupResponse> cancelOrderGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(orderGroupService.cancelOrderGroup(id, currentUser));
    }
}

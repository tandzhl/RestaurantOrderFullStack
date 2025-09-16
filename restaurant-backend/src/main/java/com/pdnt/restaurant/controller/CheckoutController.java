package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.CheckoutRequest;
import com.pdnt.restaurant.dto.response.OrderGroupResponse;
import com.pdnt.restaurant.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<OrderGroupResponse> checkout(@RequestBody CheckoutRequest req) {
        OrderGroupResponse result = checkoutService.checkout(req);
        return ResponseEntity.ok(result);
    }
}

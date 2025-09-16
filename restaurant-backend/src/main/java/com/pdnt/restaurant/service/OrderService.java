package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.request.OrderCreateRequest;
import com.pdnt.restaurant.dto.request.OrderItemRequest;
import com.pdnt.restaurant.dto.response.OrderItemResponse;
import com.pdnt.restaurant.dto.response.OrderResponse;
import com.pdnt.restaurant.entity.*;
import com.pdnt.restaurant.entity.enums.Payment;
import com.pdnt.restaurant.mapper.OrderMapper;
import com.pdnt.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. fetch user + restaurant
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new NoSuchElementException("Restaurant not found: " + request.getRestaurantId()));

        // 2. create order entity (save early to get id for txnRef if needed)
        Order order = Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .status("PENDING")
                .payment(Payment.CASH) // default
                .build();

        // allow request to set payment if provided and valid
        if (request.getPayment() != null) {
            try {
                order.setPayment(Payment.valueOf(request.getPayment().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                // invalid payment string -> keep default or throw; here we keep default
            }
        }

        order = orderRepository.save(order);

        // 3. create order items & compute total
        double total = 0.0;
        List<OrderItem> savedItems = new ArrayList<>();

        for (OrderItemRequest itReq : request.getItems()) {
            FoodItem food = foodItemRepository.findById(itReq.getFoodItemId())
                    .orElseThrow(() -> new NoSuchElementException("FoodItem not found: " + itReq.getFoodItemId()));
            if (!food.getMenu().getRestaurant().getId().equals(restaurant.getId())) {
                throw new IllegalArgumentException("FoodItem " + food.getId() + " does not belong to Restaurant " + restaurant.getId());
            }
            int qty = itReq.getQuantity() == null ? 1 : itReq.getQuantity();
            double price = (food.getPrice() == null ? 0.0 : food.getPrice()) * qty;

            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .foodItem(food)
                    .quantity(qty)
                    .price(price)
                    .build();
            OrderItem saved = orderItemRepository.save(oi);
            savedItems.add(saved);
            total += price;
        }

        // 4. update order total & save
        order.setTotalAmount(total);
        order = orderRepository.save(order);

        // 5. map to response
        OrderResponse resp = orderMapper.toOrderResponse(order);
        List<OrderItemResponse> itemResponses = savedItems.stream()
                .map(orderMapper::toOrderItemResponse)
                .collect(Collectors.toList());
        resp.setItems(itemResponses);

        return resp;
    }

    public List<OrderResponse> getOrdersByUser(Long userId) {
        List<Order> orders = orderRepository.findByCustomerId(userId);

        return orders.stream().map(order -> {
            // map order
            OrderResponse response = orderMapper.toOrderResponse(order);

            // lấy items từ OrderItemRepository
            List<OrderItemResponse> itemResponses = orderItemRepository.findByOrderId(order.getId())
                    .stream()
                    .map(orderMapper::toOrderItemResponse)
                    .toList();

            response.setItems(itemResponses);
            return response;
        }).toList();
    }

    public Page<OrderResponse> getOrdersByRestaurant(Long restaurantId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByRestaurant_Id(restaurantId, pageable);

        return orders.map(orderMapper::toOrderResponse);
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        // tìm nhà hàng chứa order này
        Restaurant restaurant = restaurantRepository.findById(order.getRestaurant().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà hàng"));

        // kiểm tra user có phải chủ nhà hàng không
        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Không phải chủ nhà hàng");
        }

        // chỉ cho hủy nếu đang PENDING
        if (!order.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Chỉ được hủy đơn ở trạng thái PENDING");
        }

        order.setStatus("REJECTED");
        orderRepository.save(order);
    }

}

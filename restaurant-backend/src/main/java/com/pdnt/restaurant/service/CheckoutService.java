package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.request.CheckoutRequest;
import com.pdnt.restaurant.dto.response.OrderGroupResponse;
import com.pdnt.restaurant.entity.*;
import com.pdnt.restaurant.entity.enums.Payment;
import com.pdnt.restaurant.exceptions.ErrorCode;
import com.pdnt.restaurant.exceptions.WebException;
import com.pdnt.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final UserRepository userRepo;
    private final RestaurantRepository restaurantRepo;
    private final FoodItemRepository foodItemRepo;
    private final OrderGroupRepository orderGroupRepo;
    private final OrderRepository orderRepo;

    @Transactional
    public OrderGroupResponse checkout(CheckoutRequest req) {
        // 1. Lấy thông tin customer
        User customer = userRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // 2. Tạo OrderGroup
        OrderGroup group = new OrderGroup();
        group.setCustomer(customer);
        group.setPayment(Payment.valueOf(req.getPayment()));
        group.setStatus("PENDING");
        group.setOrders(new ArrayList<>()); // tránh null

        // Gom item theo restaurant
        Map<Long, List<CheckoutRequest.CartItemDTO>> grouped =
                req.getItems().stream()
                        .collect(Collectors.groupingBy(CheckoutRequest.CartItemDTO::getRestaurantId));

        double total = 0;

        // 3. Tạo Order theo từng nhà hàng
        for (Map.Entry<Long, List<CheckoutRequest.CartItemDTO>> entry : grouped.entrySet()) {
            Long restaurantId = entry.getKey();
            Restaurant restaurant = restaurantRepo.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            Order order = new Order();
            order.setCustomer(customer);
            order.setRestaurant(restaurant);
            order.setOrderGroup(group);
            order.setPayment(group.getPayment());
            order.setStatus("PENDING");
            order.setOrderItems(new ArrayList<>());

            double orderTotal = 0;

            // 4. Tạo OrderItem cho từng món
            for (CheckoutRequest.CartItemDTO itemDTO : entry.getValue()) {
                FoodItem food = foodItemRepo.findById(itemDTO.getFoodItemId())
                        .orElseThrow(() -> new RuntimeException("Food not found"));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setFoodItem(food);
                orderItem.setQuantity(itemDTO.getQuantity());
                orderItem.setPrice(food.getPrice()); // giá đơn vị

                double itemTotal = food.getPrice() * itemDTO.getQuantity();
                orderTotal += itemTotal;

                order.getOrderItems().add(orderItem);
            }

            order.setTotalAmount(orderTotal);
            group.getOrders().add(order);
            total += orderTotal;
        }

        // 5. Set tổng tiền cho OrderGroup
        group.setTotalAmount(total);

        // 6. Lưu xuống DB (cascade sẽ lưu cả Orders & OrderItems)
        OrderGroup saved = orderGroupRepo.save(group);

        // 7. Convert sang response
        return OrderGroupResponse.builder()
                .id(saved.getId())
                .createdAt(saved.getCreatedAt())
                .payment(saved.getPayment())
                .status(saved.getStatus())
                .totalAmount(saved.getTotalAmount())
                .customerId(saved.getCustomer().getId())
                .build();
    }

    public List<OrderGroupResponse> getOrderGroupsByUser(Long customerId) {
        return orderGroupRepo.findByCustomer_Id(customerId)
                .stream()
                .map(group -> OrderGroupResponse.builder()
                        .id(group.getId())
                        .createdAt(group.getCreatedAt())
                        .payment(group.getPayment())
                        .status(group.getStatus())
                        .totalAmount(group.getTotalAmount())
                        .customerId(group.getCustomer().getId())
                        .build())
                .toList();
    }

    @Transactional
    public OrderGroupResponse payOrderGroup(Long groupId, User currentUser) {
        // 1. Lấy OrderGroup
        OrderGroup group = orderGroupRepo.findById(groupId)
                .orElseThrow(() -> new WebException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        // 2. Kiểm tra quyền
        if (!group.getCustomer().getId().equals(currentUser.getId())) {
            throw new WebException(ErrorCode.FORBIDDEN);
        }

        // 3. Kiểm tra group bị hủy
        if ("REJECTED".equals(group.getStatus())) {
            throw new WebException(ErrorCode.ORDER_REJECTED); // cần thêm vào ErrorCode
        }

        // 4. Kiểm tra có order nào bị hủy
        boolean hasRejected = group.getOrders().stream()
                .anyMatch(order -> "REJECTED".equals(order.getStatus()));
        if (hasRejected) {
            throw new WebException(ErrorCode.ORDER_REJECTED);
        }

        // 5. Cập nhật status
        group.setStatus("SUCCESS");
        group.getOrders().forEach(order -> order.setStatus("SUCCESS"));

        OrderGroup saved = orderGroupRepo.save(group);

        return OrderGroupResponse.builder()
                .id(saved.getId())
                .createdAt(saved.getCreatedAt())
                .payment(saved.getPayment())
                .status(saved.getStatus())
                .totalAmount(saved.getTotalAmount())
                .customerId(saved.getCustomer().getId())
                .build();
    }

    @Transactional
    public OrderGroupResponse cancelOrderGroup(Long groupId, User currentUser) {
        OrderGroup group = orderGroupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("OrderGroup not found"));

        // ✅ Kiểm tra đúng user mới được hủy
        if (!group.getCustomer().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền hủy order group này!");
        }

        // ✅ Chỉ hủy khi đơn còn PENDING
        if (!"PENDING".equals(group.getStatus())) {
            throw new RuntimeException("Không thể hủy, đơn đã xử lý hoặc thanh toán!");
        }

        // Đổi trạng thái
        group.setStatus("CANCELLED");
        group.getOrders().forEach(order -> order.setStatus("CANCELLED"));

        OrderGroup saved = orderGroupRepo.save(group);

        return OrderGroupResponse.builder()
                .id(saved.getId())
                .createdAt(saved.getCreatedAt())
                .payment(saved.getPayment())
                .status(saved.getStatus())
                .totalAmount(saved.getTotalAmount())
                .customerId(saved.getCustomer().getId())
                .build();
    }
}

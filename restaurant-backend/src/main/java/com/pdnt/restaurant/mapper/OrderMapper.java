package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.response.OrderItemResponse;
import com.pdnt.restaurant.dto.response.OrderResponse;
import com.pdnt.restaurant.dto.request.OrderItemRequest;
import com.pdnt.restaurant.entity.Order;
import com.pdnt.restaurant.entity.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    // Map Order -> OrderResponse
    @Mapping(target = "payment", expression = "java(order.getPayment() != null ? order.getPayment().name() : null)")
    @Mapping(target = "items", ignore = true) // items sẽ set thủ công
    @Mapping(target = "customerId", source = "customer.id") // 👈 thêm dòng này
    @Mapping(target = "customerName", expression = "java(order.getCustomer() != null ? order.getCustomer().getFirstName() + \" \" + order.getCustomer().getLastName() : null)")
    OrderResponse toOrderResponse(Order order);

    // Map OrderItem -> OrderItemResponse
    @Mapping(source = "foodItem.id", target = "foodItemId")
    @Mapping(source = "foodItem.name", target = "foodName")
    OrderItemResponse toOrderItemResponse(OrderItem item);
}

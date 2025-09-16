package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.response.OrderItemResponse;
import com.pdnt.restaurant.dto.response.OrderResponse;
import com.pdnt.restaurant.dto.request.OrderItemRequest;
import com.pdnt.restaurant.entity.Order;
import com.pdnt.restaurant.entity.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Map OrderItemRequest -> OrderItem
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "foodItem", ignore = true)
    @Mapping(target = "price", ignore = true)
    OrderItem toOrderItem(OrderItemRequest dto);

    // Map Order -> OrderResponse (enum -> String)
    @Mapping(target = "payment", expression = "java(order.getPayment() != null ? order.getPayment().name() : null)")
    @Mapping(target = "items", ignore = true) // 👈 bỏ qua items
    OrderResponse toOrderResponse(Order order);

    // Map OrderItem -> OrderItemResponse
    @Mapping(source = "foodItem.id", target = "foodItemId")
    @Mapping(source = "foodItem.name", target = "foodName")
    OrderItemResponse toOrderItemResponse(OrderItem item);
}
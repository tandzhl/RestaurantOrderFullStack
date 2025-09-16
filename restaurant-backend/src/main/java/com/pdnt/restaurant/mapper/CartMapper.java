package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.response.CartItemResponse;
import com.pdnt.restaurant.dto.response.CartResponse;
import com.pdnt.restaurant.entity.Cart;
import com.pdnt.restaurant.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "total", source = "items", qualifiedByName = "sumTotal")
    CartResponse toCartResponse(Cart cart);

    @Mapping(target = "foodId",   source = "foodItem.id")
    @Mapping(target = "foodName", source = "foodItem.name")
    @Mapping(target = "price",    source = "foodItem.price")
    @Mapping(
            target = "subtotal",
            expression = "java( (item.getFoodItem().getPrice() == null ? 0D : item.getFoodItem().getPrice()) * item.getQuantity() )"
    )
    CartItemResponse toCartItemResponse(CartItem item);

    // MapStruct sẽ tự dùng method trên để map List<CartItem> -> List<CartItemResponse>
    List<CartItemResponse> toCartItemResponseList(List<CartItem> items);

    @Named("sumTotal")
    default Double sumTotal(List<CartItem> items) {
        if (items == null) return 0D;
        return items.stream()
                .mapToDouble(i -> (i.getFoodItem().getPrice() == null ? 0D : i.getFoodItem().getPrice()) * i.getQuantity())
                .sum();
    }
}

package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.request.FoodItemForm;
import com.pdnt.restaurant.dto.response.FoodItemResponse;
import com.pdnt.restaurant.entity.FoodItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FoodItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "menu", ignore = true)        // xử lý thủ công sau
    @Mapping(target = "category", ignore = true)    // xử lý thủ công sau
    @Mapping(target = "imageUrl", ignore = true)    // xử lý upload riêng
    FoodItem toEntity(FoodItemForm form);

    @Mapping(target = "categoryId", source = "foodItem.category.id")
    @Mapping(target = "restaurantId", source = "foodItem.menu.restaurant.id")
    @Mapping(target = "menuId", source = "foodItem.menu.id")
    @Mapping(target = "averageRating", source = "averageRating")
    FoodItemResponse toDto(FoodItem foodItem, Double averageRating);
    List<FoodItemResponse> toResponseList(List<FoodItem> foodItems);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFoodItemFromForm(FoodItemForm form, @MappingTarget FoodItem entity);
}


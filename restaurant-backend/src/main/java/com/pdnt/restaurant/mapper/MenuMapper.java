package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.response.MenuResponse;
import com.pdnt.restaurant.entity.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuMapper {
    @Mapping(source = "restaurant.id", target = "restaurantId")
    MenuResponse toMenuResponse(Menu menu);

    List<MenuResponse> toMenuResponseList(List<Menu> menus);
}

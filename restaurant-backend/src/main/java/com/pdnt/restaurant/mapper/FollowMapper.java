package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.response.FollowResponse;
import com.pdnt.restaurant.entity.Follow;
import com.pdnt.restaurant.entity.Restaurant;
import com.pdnt.restaurant.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FollowMapper {
    FollowMapper INSTANCE = Mappers.getMapper(FollowMapper.class);

    @Mapping(target = "id", expression = "java(new FollowId(user.getId(), restaurant.getId()))")
    @Mapping(target = "customer", source = "user")
    @Mapping(target = "restaurant", source = "restaurant")
    Follow toFollow(User user, Restaurant restaurant);

    @Mapping(target = "customerId", source = "id.customerId")
    @Mapping(target = "restaurantId", source = "id.restaurantId")
    FollowResponse toResponse(Follow follow);

    List<FollowResponse> toDtoList(List<Follow> followls);
}

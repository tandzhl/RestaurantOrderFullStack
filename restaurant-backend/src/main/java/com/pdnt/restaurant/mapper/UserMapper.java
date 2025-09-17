package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.request.CreateUserRequest;
import com.pdnt.restaurant.dto.request.RegisterRequest;
import com.pdnt.restaurant.dto.request.UpdateUserRequest;
import com.pdnt.restaurant.dto.response.UserResponse;
import com.pdnt.restaurant.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterRequest request);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UpdateUserRequest request);
    UserResponse toUserResponse(User user);
}

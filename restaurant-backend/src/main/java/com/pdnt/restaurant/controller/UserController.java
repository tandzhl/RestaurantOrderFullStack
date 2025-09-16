package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.CreateUserRequest;
import com.pdnt.restaurant.dto.request.UpdateUserRequest;
import com.pdnt.restaurant.dto.response.ApiResponse;
import com.pdnt.restaurant.dto.response.UserResponse;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.mapper.UserMapper;
import com.pdnt.restaurant.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal(); // Lấy user từ SecurityContext
        return userMapper.toUserResponse(user);
    }

    @GetMapping("/{user_id}")
    User getUserById(@PathVariable("user_id") Long userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{user_id}")
    User updateUser(@PathVariable("user_id") Long userId, @RequestBody UpdateUserRequest request) {
        return userService.updateUsers(userId, request);
    }

    @DeleteMapping("/{user_id}")
    String deleteUser(@PathVariable("user_id") Long userId) {
        userService.deleteUser(userId);

        return "User has been deleted";
    }

}
package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.ChangePasswordRequest;
import com.pdnt.restaurant.dto.request.UpdateUserRequest;
import com.pdnt.restaurant.dto.response.UserResponse;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.mapper.UserMapper;
import com.pdnt.restaurant.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
        User user = (User) authentication.getPrincipal(); // lấy user từ SecurityContext
        return userMapper.toUserResponse(user);
    }

    // ✅ chỉ ADMIN và RESTAURANT_OWNER mới gọi được
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    @GetMapping("/{user_id}")
    public UserResponse getUserById(@PathVariable("user_id") Long userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{user_id}")
    public UserResponse updateUser(@PathVariable("user_id") Long userId,
                           @RequestBody UpdateUserRequest request) {
        return userService.updateUsers(userId, request);
    }

    @PutMapping("/{user_id}/change-password")
    public UserResponse changePassword(@PathVariable("user_id") Long userId,
                                 @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(userId, request.getOldPass(), request.getNewPass());
    }

    // ✅ chỉ ADMIN mới được xóa user
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{user_id}")
    public String deleteUser(@PathVariable("user_id") Long userId) {
        userService.deleteUser(userId);
        return "User has been deleted";
    }
}

package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.request.CreateNotificationRequest;
import com.pdnt.restaurant.dto.response.FoodCreateNotificationResponse;
import com.pdnt.restaurant.dto.response.NotificationResponse;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/batch")
    public ResponseEntity<List<NotificationResponse>> createBatch(@RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotifications(request));
    }

    @GetMapping("/me")
    public List<NotificationResponse> getMyNotifications() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return notificationService.getMyNotifications(currentUser.getId());
    }
    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long currentUserId = user.getId();

        return ResponseEntity.ok(notificationService.markAsRead(id, currentUserId));
    }
}

package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.request.CreateNotificationRequest;
import com.pdnt.restaurant.dto.response.FoodCreateNotificationResponse;
import com.pdnt.restaurant.dto.response.NotificationResponse;
import com.pdnt.restaurant.entity.FoodNotification;
import com.pdnt.restaurant.entity.Notification;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.mapper.NotificationMapper;
import com.pdnt.restaurant.repository.FoodNotificationRepository;
import com.pdnt.restaurant.repository.NotificationRepository;
import com.pdnt.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FoodNotificationRepository foodNotificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public List<NotificationResponse> createNotifications(CreateNotificationRequest request) {
        List<NotificationResponse> responses = new ArrayList<>();

        for (Long userId : request.getUserIds()) {
            User recipient = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            Notification notification = Notification.builder()
                    .recipient(recipient)
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type(request.getType())
                    .build();

            Notification saved = notificationRepository.save(notification);
            responses.add(notificationMapper.toNotificationResponse(saved));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(notificationMapper::toNotificationResponsePolymorphic)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new RuntimeException("You are not the owner of this notification");
        }

        notification.setRead(true);
        return notificationMapper.toNotificationResponse(notificationRepository.save(notification));
    }

    @Transactional
    public List<FoodCreateNotificationResponse> createFoodNotifications(
            List<Long> userIds, String title, String message, Long foodId) {

        List<FoodCreateNotificationResponse> responses = new ArrayList<>();

        for (Long userId : userIds) {
            User recipient = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            FoodNotification foodNotification = FoodNotification.builder()
                    .recipient(recipient)
                    .title(title)
                    .message(message)
                    .type("NEW_FOOD")
                    .foodId(foodId)
                    .build();

            FoodNotification saved = foodNotificationRepository.save(foodNotification);
            responses.add(notificationMapper.toFoodNotificationResponse(saved));
        }

        return responses;
    }
}

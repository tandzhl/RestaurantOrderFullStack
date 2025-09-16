package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.dto.response.FoodCreateNotificationResponse;
import com.pdnt.restaurant.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId);
}

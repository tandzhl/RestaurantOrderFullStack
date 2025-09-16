package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.FoodNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodNotificationRepository extends JpaRepository<FoodNotification, Long> {
    List<FoodNotification> findByRecipient_IdOrderByCreatedAtDesc(Long recipientId);
}

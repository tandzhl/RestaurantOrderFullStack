package com.pdnt.restaurant.mapper;

import com.pdnt.restaurant.dto.response.FoodCreateNotificationResponse;
import com.pdnt.restaurant.dto.response.NotificationResponse;
import com.pdnt.restaurant.entity.FoodNotification;
import com.pdnt.restaurant.entity.Notification;
import com.pdnt.restaurant.entity.RestaurantNotification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "recipient.id", target = "recipientId")
    NotificationResponse toNotificationResponse(Notification entity);

    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "foodId", target = "foodItemId")
    FoodCreateNotificationResponse toFoodNotificationResponse(FoodNotification entity);

    // ✅ Hàm default xử lý subclass
    default NotificationResponse toNotificationResponsePolymorphic(Notification entity) {
        NotificationResponse response = toNotificationResponse(entity);

        if (entity instanceof FoodNotification foodNoti) {
            response.setFoodId(foodNoti.getFoodId());
        } else if (entity instanceof RestaurantNotification restNoti) {
            response.setRestaurantId(restNoti.getRestaurantId());
        }

        // nếu không thuộc subclass nào thì cả 2 sẽ null
        return response;
    }
}

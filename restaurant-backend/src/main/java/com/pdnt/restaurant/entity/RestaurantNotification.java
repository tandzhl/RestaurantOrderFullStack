package com.pdnt.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("RESTAURANT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // ✅ nên có nếu cha Notification cũng dùng @SuperBuilder
public class RestaurantNotification extends Notification {
    @Column(name = "restaurant_id")
    private Long restaurantId;
}
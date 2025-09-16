package com.pdnt.restaurant.entity.composite_keys;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowId implements Serializable {
    private Long customerId;
    private Long restaurantId;
}

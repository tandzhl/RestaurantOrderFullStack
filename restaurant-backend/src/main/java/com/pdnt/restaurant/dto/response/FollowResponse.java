package com.pdnt.restaurant.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowResponse {
    private Long customerId;
    private Long restaurantId;

}

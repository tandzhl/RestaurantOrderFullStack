package com.pdnt.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserConversationDTO {
    private Long userId;
    private String fullName;
    private ChatMessageResponse lastMessage;
}
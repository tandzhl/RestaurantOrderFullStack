package com.pdnt.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageResponse {
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String message;
    private Long timestamp;
}

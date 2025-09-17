package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.response.ChatMessageResponse;
import com.pdnt.restaurant.dto.response.UserConversationDTO;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    // ✅ User gửi tin nhắn tới nhà hàng
    @PostMapping("/user/send")
    public String sendMessageFromUser(@AuthenticationPrincipal User sender,
                                      @RequestParam Long restaurantId,
                                      @RequestParam String message) throws ExecutionException, InterruptedException {
        return chatService.saveMessageFromUser(sender, restaurantId, message);
    }

    // ✅ Owner gửi tin nhắn từ nhà hàng
    @PostMapping("/restaurant/send")
    public String sendMessageAsRestaurant(@AuthenticationPrincipal User owner,
                                          @RequestParam Long restaurantId,
                                          @RequestParam Long receiverId,
                                          @RequestParam String message) throws ExecutionException, InterruptedException {
        return chatService.saveMessageAsRestaurant(owner, restaurantId, receiverId, message);
    }

    // ✅ User lấy tin nhắn với một nhà hàng
    @GetMapping("/user/{restaurantId}/messages")
    public List<ChatMessageResponse> getMessagesWithRestaurant(@AuthenticationPrincipal User user,
                                                               @PathVariable Long restaurantId)
            throws ExecutionException, InterruptedException {
        return chatService.getMessagesWithRestaurant(user.getId(), restaurantId);
    }

    // ✅ Restaurant owner lấy tin nhắn với một user
    @GetMapping("/restaurant/{restaurantId}/messages/{userId}")
    public List<ChatMessageResponse> getMessagesWithUser(@AuthenticationPrincipal User owner,
                                                         @PathVariable Long restaurantId,
                                                         @PathVariable Long userId)
            throws ExecutionException, InterruptedException {
        return chatService.getMessagesWithUser(restaurantId, userId);
    }

    @GetMapping("/restaurant/{restaurantId}/conversations")
    public List<UserConversationDTO> getConversations(@PathVariable Long restaurantId)
            throws ExecutionException, InterruptedException {
        return chatService.getConversationsForRestaurant(restaurantId);
    }
}

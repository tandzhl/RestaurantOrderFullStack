package com.pdnt.restaurant.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.pdnt.restaurant.dto.response.ChatMessageResponse;
import com.pdnt.restaurant.dto.response.UserConversationDTO;
import com.pdnt.restaurant.entity.Restaurant;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.repository.RestaurantRepository;
import com.pdnt.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private static final String COLLECTION_NAME = "messages";

    // ✅ User gửi tin nhắn tới nhà hàng
    // ✅ User gửi tin nhắn tới nhà hàng
    public String saveMessageFromUser(User sender, Long restaurantId, String message)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Lấy thông tin nhà hàng
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng"));

        // Receiver là owner của nhà hàng
        Long receiverId = restaurant.getOwner().getId();

        Map<String, Object> chat = new HashMap<>();
        chat.put("senderId", sender.getId());
        chat.put("senderName", sender.getFirstName() + " " + sender.getLastName());
        chat.put("restaurantId", restaurantId);
        chat.put("receiverId", receiverId);
        chat.put("message", message);
        chat.put("timestamp", System.currentTimeMillis());

        ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(chat);
        return future.get().getId();
    }


    // ✅ Owner gửi tin nhắn từ một nhà hàng
    public String saveMessageAsRestaurant(User owner, Long restaurantId, Long receiverId, String message)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng"));

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Bạn không có quyền gửi tin nhắn từ nhà hàng này");
        }

        Map<String, Object> chat = new HashMap<>();
        chat.put("senderId", owner.getId());
        chat.put("senderName", restaurant.getName());
        chat.put("restaurantId", restaurantId);
        chat.put("receiverId", receiverId);
        chat.put("message", message);
        chat.put("timestamp", System.currentTimeMillis());

        ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(chat);
        return future.get().getId();
    }

    // ✅ User lấy lịch sử chat với một nhà hàng
    public List<ChatMessageResponse> getMessagesWithRestaurant(Long userId, Long restaurantId)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        Query query = db.collection(COLLECTION_NAME)
                .whereEqualTo("restaurantId", restaurantId)
                .where(Filter.or(
                        Filter.equalTo("senderId", userId),
                        Filter.equalTo("receiverId", userId)
                ))
                .orderBy("timestamp", Query.Direction.ASCENDING);

        ApiFuture<QuerySnapshot> future = query.get();

        return future.get().getDocuments().stream()
                .map(doc -> new ChatMessageResponse(
                        doc.getLong("senderId"),
                        doc.getString("senderName"),
                        doc.getLong("receiverId"),
                        doc.getString("message"),
                        doc.getLong("timestamp")
                ))
                .toList();
    }

    // ✅ Restaurant owner lấy lịch sử chat với một user
    public List<ChatMessageResponse> getMessagesWithUser(Long restaurantId, Long userId)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        Query query = db.collection(COLLECTION_NAME)
                .whereEqualTo("restaurantId", restaurantId)
                .where(Filter.or(
                        Filter.equalTo("senderId", userId),
                        Filter.equalTo("receiverId", userId)
                ))
                .orderBy("timestamp", Query.Direction.ASCENDING);

        ApiFuture<QuerySnapshot> future = query.get();

        return future.get().getDocuments().stream()
                .map(doc -> new ChatMessageResponse(
                        doc.getLong("senderId"),
                        doc.getString("senderName"),
                        doc.getLong("receiverId"),
                        doc.getString("message"),
                        doc.getLong("timestamp")
                ))
                .toList();
    }

    public List<UserConversationDTO> getConversationsForRestaurant(Long restaurantId)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Lấy ownerId của nhà hàng
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà hàng"));
        Long ownerId = restaurant.getOwner().getId();

        // Query lấy tất cả tin nhắn của nhà hàng
        Query query = db.collection(COLLECTION_NAME)
                .whereEqualTo("restaurantId", restaurantId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        ApiFuture<QuerySnapshot> future = query.get();
        List<QueryDocumentSnapshot> docs = future.get().getDocuments();

        Map<Long, ChatMessageResponse> lastMessageByUser = new HashMap<>();

        for (QueryDocumentSnapshot doc : docs) {
            Long senderId = doc.contains("senderId") ? doc.getLong("senderId") : null;
            Long receiverId = doc.contains("receiverId") ? doc.getLong("receiverId") : null;

            if (senderId == null || receiverId == null) continue;

            // Lấy userId khác với owner
            Long userId = senderId.equals(ownerId) ? receiverId : senderId;

            if (userId == null) continue;

            if (!lastMessageByUser.containsKey(userId)) {
                // Convert timestamp an toàn
                Object tsObj = doc.get("timestamp");
                Long ts;
                if (tsObj instanceof Number) {
                    ts = ((Number) tsObj).longValue();
                } else if (tsObj instanceof com.google.cloud.Timestamp) {
                    ts = ((com.google.cloud.Timestamp) tsObj).toDate().getTime();
                } else {
                    ts = System.currentTimeMillis();
                }

                lastMessageByUser.put(userId, new ChatMessageResponse(
                        senderId,
                        doc.getString("senderName"),
                        receiverId,
                        doc.getString("message"),
                        ts
                ));
            }
        }

        return lastMessageByUser.entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    ChatMessageResponse lastMessage = entry.getValue();

                    // Lấy thông tin user từ DB
                    User user = userRepository.findById(userId)
                            .orElse(null);

                    return new UserConversationDTO(
                            userId,
                            user != null ? user.getFirstName() + " " + user.getLastName() : "Khách hàng",
                            lastMessage
                    );
                })
                .collect(Collectors.toList());
    }
}

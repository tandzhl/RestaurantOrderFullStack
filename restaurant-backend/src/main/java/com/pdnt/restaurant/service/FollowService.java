package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.response.FollowResponse;
import com.pdnt.restaurant.entity.Follow;
import com.pdnt.restaurant.entity.Restaurant;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.entity.composite_keys.FollowId;
import com.pdnt.restaurant.mapper.FollowMapper;
import com.pdnt.restaurant.repository.FollowRepository;
import com.pdnt.restaurant.repository.RestaurantRepository;
import com.pdnt.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final FollowMapper followMapper;

    public FollowResponse follow(Long userId, Long restaurantId) {
        FollowId followId = new FollowId(userId, restaurantId);

        if(followRepository.existsById(followId)) {
            throw new RuntimeException("User already followed this restaurant");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Follow follow = followMapper.toFollow(user, restaurant);
        followRepository.save(follow);

        return followMapper.toResponse(follow);
    }

    public void unfollowRestaurant(Long userId, Long restaurantId) {
        FollowId followId = new FollowId(userId, restaurantId);

        if (!followRepository.existsById(followId)) {
            throw new RuntimeException("User does not follow this restaurant");
        }

        followRepository.deleteById(followId);
    }

    public List<FollowResponse> getFollowsByUser(Long userId) {
        return followRepository.findByCustomer_Id(userId)
                .stream()
                .map(followMapper::toResponse)
                .toList();
    }

    public List<FollowResponse> getFollowers(Long restaurantId) {
        return followMapper.toDtoList(followRepository.findByRestaurantId(restaurantId));
    }

    public boolean isFollowing(Long customerId, Long restaurantId) {
        FollowId followId = new FollowId(customerId, restaurantId);
        return followRepository.existsById(followId);
    }
}

package com.pdnt.restaurant.controller;

import com.pdnt.restaurant.dto.response.FollowResponse;
import com.pdnt.restaurant.entity.User;
import com.pdnt.restaurant.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;
    @PostMapping("/{restaurantId}")
    public ResponseEntity<FollowResponse> follow(
            @PathVariable Long restaurantId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal(); // user đang login
        return ResponseEntity.ok(followService.follow(user.getId(), restaurantId));
    }

    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<String> unfollow(
            @PathVariable Long restaurantId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        followService.unfollowRestaurant(user.getId(), restaurantId);
        return ResponseEntity.ok("Unfollowed successfully");
    }

    @GetMapping("/my-follows")
    public ResponseEntity<List<FollowResponse>> getMyFollows(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(followService.getFollowsByUser(user.getId()));
    }

    @GetMapping("/restaurant/{restaurantId}/followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(followService.getFollowers(restaurantId));
    }

    @GetMapping("/{restaurantId}/is-following")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal User currentUser
    ) {
        boolean result = followService.isFollowing(currentUser.getId(), restaurantId);
        return ResponseEntity.ok(result);
    }
}

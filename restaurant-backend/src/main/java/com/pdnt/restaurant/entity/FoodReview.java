package com.pdnt.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "food_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Double rating;
    @Column(nullable = false, length = 255)
    private String comment;
    @Column(nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();
    private String imageUrl;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;
}

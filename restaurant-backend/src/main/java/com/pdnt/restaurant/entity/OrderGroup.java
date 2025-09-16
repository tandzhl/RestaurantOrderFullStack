package com.pdnt.restaurant.entity;

import com.pdnt.restaurant.entity.enums.Payment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private Payment payment;

    private String status; // PENDING, PAID...

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "orderGroup", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();
}

package com.pdnt.restaurant.entity;

import com.pdnt.restaurant.entity.enums.Payment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false) // 🔹 Gắn trực tiếp với 1 nhà hàng
    private Restaurant restaurant;

    private Double totalAmount;

    private String status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Payment payment;
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean isRefunded = false;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private RefundRequest refundRequest;

    @ManyToOne
    @JoinColumn(name = "order_group_id")
    private OrderGroup orderGroup;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderItem> orderItems = new java.util.ArrayList<>();
}

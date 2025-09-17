package com.pdnt.restaurant.repository;

import com.pdnt.restaurant.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);

    Page<Order> findByRestaurant_Id(Long restaurantId, Pageable pageable);

    // Doanh thu theo tháng (tổng tiền và số đơn hàng success)
    @Query("SELECT MONTH(o.createdAt), YEAR(o.createdAt), COUNT(o), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.status = 'SUCCESS' AND o.restaurant.id = :restaurantId " +
            "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
            "ORDER BY YEAR(o.createdAt), MONTH(o.createdAt)")
    List<Object[]> getRevenueByMonth(@Param("restaurantId") Long restaurantId);

    // Doanh thu + số đơn theo năm cho 1 nhà hàng
    @Query("SELECT YEAR(o.createdAt), COUNT(o), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.status = 'SUCCESS' AND o.restaurant.id = :restaurantId " +
            "GROUP BY YEAR(o.createdAt) " +
            "ORDER BY YEAR(o.createdAt)")
    List<Object[]> getRevenueByYear(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COUNT(o), SUM(o.totalAmount) " +
            "FROM Order o " +
            "WHERE o.status = 'SUCCESS' " +
            "AND o.restaurant.id = :restaurantId " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Object[]> getRevenueByDateRange(@Param("restaurantId") Long restaurantId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);


}

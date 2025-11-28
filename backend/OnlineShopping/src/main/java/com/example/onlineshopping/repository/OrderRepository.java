package com.example.onlineshopping.repository;

import com.example.onlineshopping.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatus(Order.OrderStatus status);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems LEFT JOIN FETCH o.user ORDER BY o.createTime DESC")
    List<Order> findAllWithOrderItems();
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId ORDER BY o.createTime DESC")
    List<Order> findByUserIdWithOrderItems(Long userId);
    
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithOrderItems(Long orderId);
}
package com.example.onlineshopping.repository;

import com.example.onlineshopping.dto.ProductSalesDto;
import com.example.onlineshopping.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT new com.example.onlineshopping.dto.ProductSalesDto(p.name, SUM(oi.quantity)) " +
           "FROM OrderItem oi " +
           "JOIN oi.product p " +
           "GROUP BY p.id, p.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<ProductSalesDto> findTopSellingProducts(Pageable pageable);
}
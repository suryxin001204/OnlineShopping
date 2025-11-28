package com.example.onlineshopping.repository;

import com.example.onlineshopping.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatusTrue();
    List<Product> findByCategoryIdAndStatusTrue(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.status = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    List<Product> findByCategoryIdInAndStatusTrue(List<Long> categoryIds);
}
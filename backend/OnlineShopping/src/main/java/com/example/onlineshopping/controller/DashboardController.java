package com.example.onlineshopping.controller;

import com.example.onlineshopping.dto.ProductSalesDto;
import com.example.onlineshopping.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final OrderItemRepository orderItemRepository;

    @GetMapping("/top-products")
    public ResponseEntity<List<ProductSalesDto>> getTopSellingProducts() {
        // 获取前 10 名
        List<ProductSalesDto> topProducts = orderItemRepository.findTopSellingProducts(PageRequest.of(0, 10));
        return ResponseEntity.ok(topProducts);
    }
}

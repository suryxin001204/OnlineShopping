package com.example.onlineshopping.controller;

import com.example.onlineshopping.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 获取最近30天的销售趋势数据
     */
    @GetMapping("/sales-trend")
    public ResponseEntity<?> getSalesTrend() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        // 获取最近30天的订单
        var orders = orderRepository.findAll().stream()
                .filter(order -> order.getCreateTime() != null && order.getCreateTime().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());
        
        // 按日期分组统计
        Map<LocalDate, Double> salesByDate = new TreeMap<>();
        for (int i = 0; i < 30; i++) {
            LocalDate date = LocalDate.now().minusDays(29 - i);
            salesByDate.put(date, 0.0);
        }
        
        orders.forEach(order -> {
            LocalDate date = order.getCreateTime().toLocalDate();
            if (salesByDate.containsKey(date)) {
                salesByDate.put(date, salesByDate.get(date) + order.getTotalAmount().doubleValue());
            }
        });
        
        List<String> dates = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        salesByDate.forEach((date, amount) -> {
            dates.add(date.toString());
            amounts.add(amount);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("amounts", amounts);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取分类销售占比
     */
    @GetMapping("/category-sales")
    public ResponseEntity<?> getCategorySales() {
        var products = productRepository.findAll();
        var categories = categoryRepository.findAll();
        
        Map<String, Double> categorySales = new HashMap<>();
        
        // 初始化所有分类
        categories.forEach(category -> {
            categorySales.put(category.getName(), 0.0);
        });
        
        // 统计每个分类的销售额（基于产品价格 * 库存变化作为简化）
        products.forEach(product -> {
            if (product.getCategory() != null) {
                String categoryName = product.getCategory().getName();
                // 这里简化处理，实际应该从订单项中统计
                double sales = product.getPrice().doubleValue() * (100 - product.getStock());
                categorySales.put(categoryName, categorySales.getOrDefault(categoryName, 0.0) + sales);
            }
        });
        
        List<Map<String, Object>> data = new ArrayList<>();
        categorySales.forEach((name, value) -> {
            if (value > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", name);
                item.put("value", value);
                data.add(item);
            }
        });
        
        return ResponseEntity.ok(data);
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/user-growth")
    public ResponseEntity<?> getUserGrowth() {
        var users = userRepository.findAll();
        
        // 按月统计用户注册数
        Map<String, Long> usersByMonth = new TreeMap<>();
        
        users.forEach(user -> {
            if (user.getCreateTime() != null) {
                String month = user.getCreateTime().getYear() + "-" + 
                              String.format("%02d", user.getCreateTime().getMonthValue());
                usersByMonth.put(month, usersByMonth.getOrDefault(month, 0L) + 1);
            }
        });
        
        // 计算累计用户数
        List<String> months = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> totalUsers = new ArrayList<>();
        
        long cumulative = 0;
        for (Map.Entry<String, Long> entry : usersByMonth.entrySet()) {
            months.add(entry.getKey());
            newUsers.add(entry.getValue());
            cumulative += entry.getValue();
            totalUsers.add(cumulative);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("months", months);
        result.put("newUsers", newUsers);
        result.put("totalUsers", totalUsers);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取热销商品 TOP 10
     */
    @GetMapping("/top-products")
    public ResponseEntity<?> getTopProducts() {
        var products = productRepository.findAll();
        
        // 根据销量（库存减少量）排序，这里简化处理
        List<Map<String, Object>> topProducts = products.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getSales(), p1.getSales()))
                .limit(10)
                .map(product -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", product.getName());
                    item.put("sales", product.getSales());
                    return item;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(topProducts);
    }

    /**
     * 获取订单状态分布
     */
    @GetMapping("/order-status")
    public ResponseEntity<?> getOrderStatus() {
        var orders = orderRepository.findAll();
        
        Map<String, Long> statusCount = orders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getStatus().toString(),
                    Collectors.counting()
                ));
        
        List<Map<String, Object>> data = new ArrayList<>();
        statusCount.forEach((status, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", getStatusName(status));
            item.put("value", count);
            data.add(item);
        });
        
        return ResponseEntity.ok(data);
    }
    
    private String getStatusName(String status) {
        switch (status) {
            case "PENDING": return "待处理";
            case "PAID": return "已支付";
            case "SHIPPED": return "已发货";
            case "DELIVERED": return "已送达";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
}

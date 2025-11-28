package com.example.onlineshopping.controller;

import com.example.onlineshopping.dto.OrderDto;
import com.example.onlineshopping.entity.Order;
import com.example.onlineshopping.entity.User;
import com.example.onlineshopping.service.OrderService;
import com.example.onlineshopping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Getting orders for user: " + userDetails.getUsername());
        Long userId = getUserIdFromUserDetails(userDetails);
        System.out.println("Resolved user ID: " + userId);
        List<Order> orders = orderService.findByUserId(userId);
        System.out.println("Found " + orders.size() + " orders");
        return ResponseEntity.ok(orders.stream().map(OrderDto::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(orders.stream().map(OrderDto::fromEntity).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String shippingAddress,
            @RequestParam String paymentMethod) {
        System.out.println("Creating order for user: " + userDetails.getUsername());
        Long userId = getUserIdFromUserDetails(userDetails);
        System.out.println("Resolved user ID: " + userId);
        Order order = orderService.createOrder(userId, shippingAddress, paymentMethod);
        System.out.println("Order created with ID: " + order.getId());
        return ResponseEntity.ok(OrderDto.fromEntity(order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        Order order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(OrderDto.fromEntity(order));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderDto> payOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("User " + userDetails.getUsername() + " paying for order: " + id);
        Long userId = getUserIdFromUserDetails(userDetails);
        Order order = orderService.payOrder(id, userId);
        System.out.println("Order " + id + " payment completed");
        return ResponseEntity.ok(OrderDto.fromEntity(order));
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        System.out.println("Resolving User ID for username: " + userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
        return user.getId();
    }
}
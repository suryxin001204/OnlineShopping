package com.example.onlineshopping.service;

import com.example.onlineshopping.entity.*;
import com.example.onlineshopping.repository.CartItemRepository;
import com.example.onlineshopping.repository.OrderRepository;
import com.example.onlineshopping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, CartItemRepository cartItemRepository, CartService cartService, ProductService productService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
        this.productService = productService;
    }

    /**
     * 根据用户ID获取订单列表
     */
    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserIdWithOrderItems(userId);
    }

    /**
     * 获取所有订单
     */
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllWithOrderItems();
    }

    /**
     * 根据ID获取订单
     */
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * 根据订单号获取订单
     */
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    /**
     * 根据状态获取订单
     */
    public List<Order> findByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * 创建订单
     */
    @Transactional
    public Order createOrder(Long userId, String shippingAddress, String paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        // 检查库存
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProduct().getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("商品 '" + cartItem.getProduct().getName() + "' 库存不足，当前库存: " + cartItem.getProduct().getStock());
            }
        }

        // 计算总金额
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建订单
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 添加订单项并更新库存
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setSubtotal(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            order.getOrderItems().add(orderItem);

            // 更新商品库存
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            if (product.getStock() == 0) {
                product.setStatus(false); // 库存为0时自动下架
            }
            productService.save(product);
        }

        // 保存订单并清空购物车
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(userId);

        return savedOrder;
    }

    /**
     * 更新订单状态
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        return orderRepository.findByIdWithOrderItems(orderId)
                .map(order -> {
                    Order.OrderStatus oldStatus = order.getStatus();
                    order.setStatus(status);
                    order.setUpdateTime(LocalDateTime.now());

                    // 如果订单取消，恢复库存
                    if (status == Order.OrderStatus.CANCELLED && oldStatus != Order.OrderStatus.CANCELLED) {
                        restoreStock(order);
                    }

                    return orderRepository.save(order);
                })
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    /**
     * 用户支付订单
     */
    @Transactional
    public Order payOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        
        // 验证订单属于该用户
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        
        // 只有待支付状态的订单才能支付
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }
        
        // 更新订单状态为已支付
        order.setStatus(Order.OrderStatus.PAID);
        order.setUpdateTime(LocalDateTime.now());
        
        System.out.println("订单 " + orderId + " 支付成功，状态更新为: PAID");
        return orderRepository.save(order);
    }

    /**
     * 取消订单
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
    }

    /**
     * 删除订单
     */
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 只有取消的订单才能删除
        if (order.getStatus() != Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("只能删除已取消的订单");
        }

        orderRepository.delete(order);
    }

    /**
     * 获取用户订单统计
     */
    public OrderStatistics getUserOrderStatistics(Long userId) {
        List<Order> userOrders = orderRepository.findByUserIdOrderByCreateTimeDesc(userId);

        OrderStatistics stats = new OrderStatistics();
        stats.setTotalOrders(userOrders.size());
        stats.setPendingOrders((int) userOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PENDING).count());
        stats.setPaidOrders((int) userOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.PAID).count());
        stats.setShippedOrders((int) userOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.SHIPPED).count());
        stats.setDeliveredOrders((int) userOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED).count());
        stats.setCancelledOrders((int) userOrders.stream().filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED).count());

        BigDecimal totalAmount = userOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalAmount(totalAmount);

        return stats;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 恢复库存（订单取消时调用）
     */
    private void restoreStock(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
            if (!product.getStatus() && product.getStock() > 0) {
                product.setStatus(true); // 如果之前因为库存为0下架，现在恢复上架
            }
            productService.save(product);
        }
    }

    /**
     * 订单统计信息
     */
    public static class OrderStatistics {
        private int totalOrders;
        private int pendingOrders;
        private int paidOrders;
        private int shippedOrders;
        private int deliveredOrders;
        private int cancelledOrders;
        private BigDecimal totalAmount;

        // Getters and Setters
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public int getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }

        public int getPaidOrders() { return paidOrders; }
        public void setPaidOrders(int paidOrders) { this.paidOrders = paidOrders; }

        public int getShippedOrders() { return shippedOrders; }
        public void setShippedOrders(int shippedOrders) { this.shippedOrders = shippedOrders; }

        public int getDeliveredOrders() { return deliveredOrders; }
        public void setDeliveredOrders(int deliveredOrders) { this.deliveredOrders = deliveredOrders; }

        public int getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}
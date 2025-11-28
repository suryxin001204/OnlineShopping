package com.example.onlineshopping.service;

import com.example.onlineshopping.dto.CartItemDto;
import com.example.onlineshopping.entity.CartItem;
import com.example.onlineshopping.entity.Product;
import com.example.onlineshopping.entity.User;
import com.example.onlineshopping.repository.CartItemRepository;
import com.example.onlineshopping.repository.ProductRepository;
import com.example.onlineshopping.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * 获取用户的购物车商品列表
     */
    public List<CartItemDto> getCartItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 添加商品到购物车
     */
    @Transactional
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 检查库存
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存: " + product.getStock());
        }

        // 检查商品状态
        if (!product.getStatus()) {
            throw new RuntimeException("商品已下架");
        }

        // 检查是否已在购物车
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .map(cartItem -> {
                    // 检查更新后的数量是否超过库存
                    int newQuantity = cartItem.getQuantity() + quantity;
                    if (product.getStock() < newQuantity) {
                        throw new RuntimeException("库存不足，当前库存: " + product.getStock() + "，购物车已有: " + cartItem.getQuantity());
                    }

                    // 更新数量
                    cartItem.setQuantity(newQuantity);
                    cartItem.setUpdateTime(LocalDateTime.now());
                    return cartItemRepository.save(cartItem);
                })
                .orElseGet(() -> {
                    // 新增购物车项
                    CartItem cartItem = new CartItem();
                    cartItem.setUser(user);
                    cartItem.setProduct(product);
                    cartItem.setQuantity(quantity);
                    cartItem.setCreateTime(LocalDateTime.now());
                    cartItem.setUpdateTime(LocalDateTime.now());
                    return cartItemRepository.save(cartItem);
                });
    }

    /**
     * 更新购物车商品数量
     */
    @Transactional
    public CartItem updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(userId, productId);
            return null;
        }

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("购物车商品不存在"));

        // 检查库存
        if (cartItem.getProduct().getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存: " + cartItem.getProduct().getStock());
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdateTime(LocalDateTime.now());
        return cartItemRepository.save(cartItem);
    }

    /**
     * 从购物车移除商品
     */
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    /**
     * 清空用户购物车
     */
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    /**
     * 获取购物车商品数量
     */
    public Integer getCartItemCount(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * 计算购物车总金额
     */
    public Double getCartTotalAmount(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    /**
     * 检查购物车商品库存
     */
    public boolean checkCartStock(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        for (CartItem item : cartItems) {
            if (item.getProduct().getStock() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 转换实体为DTO
     */
    private CartItemDto convertToDto(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductImage(cartItem.getProduct().getImageUrl());
        dto.setProductPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setUpdateTime(cartItem.getUpdateTime());
        return dto;
    }
}
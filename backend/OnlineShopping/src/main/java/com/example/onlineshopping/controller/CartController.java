package com.example.onlineshopping.controller;

import com.example.onlineshopping.dto.CartItemDto;
import com.example.onlineshopping.entity.CartItem;
import com.example.onlineshopping.entity.User;
import com.example.onlineshopping.service.CartService;
import com.example.onlineshopping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/items")
    public ResponseEntity<List<CartItemDto>> getCartItems(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Long userId = getUserIdFromUserDetails(userDetails);
        try {
            CartItem cartItem = cartService.addToCart(userId, productId, quantity);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/items")
    public ResponseEntity<?> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        Long userId = getUserIdFromUserDetails(userDetails);
        try {
            CartItem cartItem = cartService.updateCartItemQuantity(userId, productId, quantity);
            return ResponseEntity.ok(cartItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        Long userId = getUserIdFromUserDetails(userDetails);
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        System.out.println("[CartController] Resolving User ID for username: " + userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + userDetails.getUsername()));
        System.out.println("[CartController] User ID resolved: " + user.getId());
        return user.getId();
    }
}
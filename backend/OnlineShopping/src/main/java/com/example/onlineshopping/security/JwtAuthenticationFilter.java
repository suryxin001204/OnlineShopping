package com.example.onlineshopping.security;

import com.example.onlineshopping.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String username = null;
            String jwtToken = null;

            // 提取JWT令牌
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
                try {
                    username = jwtUtil.extractUsername(jwtToken);
                } catch (ExpiredJwtException e) {
                    log.warn("JWT令牌已过期: {}", e.getMessage());
                } catch (MalformedJwtException e) {
                    log.warn("JWT令牌格式错误: {}", e.getMessage());
                } catch (SignatureException e) {
                    log.warn("JWT令牌签名无效: {}", e.getMessage());
                } catch (Exception e) {
                    log.error("JWT令牌解析错误: {}", e.getMessage());
                }
            }

            // 验证令牌并设置认证信息
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = userService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("用户 {} 认证成功，权限: {}", username, userDetails.getAuthorities());
                    }
                } catch (UsernameNotFoundException e) {
                    log.warn("用户不存在: {}", username);
                } catch (Exception e) {
                    log.error("用户认证失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("JWT过滤器处理异常: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
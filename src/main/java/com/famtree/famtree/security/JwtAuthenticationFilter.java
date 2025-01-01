package com.famtree.famtree.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import com.famtree.famtree.entity.User;
import com.famtree.famtree.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            
            // List of paths to skip authentication
            if (path.startsWith("/api/auth/") || 
                path.equals("/api/members/register") ||
                path.equals("/api/members/verify") ||
                path.equals("/api/members/verify-family-code") ||
                path.equals("/error")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = getTokenFromRequest(request);
            if (token == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "No token provided", request);
                return;
            }

            System.out.println("Processing request for path: " + path);
            System.out.println("Token received: " + token);

            String mobile = jwtUtil.getMobileFromToken(token);
            System.out.println("Mobile from token: " + mobile);

            // Try to find user with retries
            User user = null;
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    user = userRepository.findByMobile(mobile).orElse(null);
                    if (user != null) break;
                    System.out.println("Retry " + (i + 1) + ": User not found, waiting...");
                    Thread.sleep(1000); // Wait 1 second before retry
                } catch (Exception e) {
                    System.out.println("Error finding user on try " + (i + 1) + ": " + e.getMessage());
                }
            }

            if (user == null) {
                System.out.println("User not found after " + maxRetries + " retries for mobile: " + mobile);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "User not found. Please try logging in again.", request);
                return;
            }

            System.out.println("Found user: " + user.getFirstName() + " with mobile: " + user.getMobile());

            // Verify token matches stored token
            if (!token.equals(user.getCurrentToken())) {
                System.out.println("Token mismatch. Stored: " + user.getCurrentToken());
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "Invalid token. Please login again.", request);
                return;
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(user.getRole().toString()));

            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(mobile, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                "Authentication failed: " + e.getMessage(), request);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message, HttpServletRequest request) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", message);
        errorDetails.put("timestamp", new Date());
        errorDetails.put("path", request.getRequestURI());

        String jsonResponse = new ObjectMapper().writeValueAsString(errorDetails);
        response.getWriter().write(jsonResponse);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 
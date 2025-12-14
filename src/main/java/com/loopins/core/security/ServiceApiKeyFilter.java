package com.loopins.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter to validate service-to-service API key for protected endpoints.
 * Protected endpoints require X-SERVICE-KEY header.
 */
@Slf4j
@Component
public class ServiceApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-SERVICE-KEY";

    @Value("${service.api-key}")
    private String expectedApiKey;

    // Endpoints that require service API key
    private static final List<String> PROTECTED_PATTERNS = List.of(
            "/api/orders/.*/payment-confirmed",
            "/api/orders/.*/payment-failed"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Check if this is a protected endpoint
        if (isProtectedEndpoint(requestPath)) {
            String apiKey = request.getHeader(API_KEY_HEADER);

            if (apiKey == null || apiKey.isBlank()) {
                log.warn("Missing API key for protected endpoint: {}", requestPath);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"error\":\"Missing service API key\"}");
                return;
            }

            if (!expectedApiKey.equals(apiKey)) {
                log.warn("Invalid API key for protected endpoint: {}", requestPath);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"error\":\"Invalid service API key\"}");
                return;
            }

            log.debug("Valid API key for protected endpoint: {}", requestPath);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProtectedEndpoint(String path) {
        return PROTECTED_PATTERNS.stream()
                .anyMatch(pattern -> path.matches(pattern));
    }
}


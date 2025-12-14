package com.loopins.core.controller;

import com.loopins.core.dto.request.PaymentConfirmationRequest;
import com.loopins.core.dto.response.ApiResponse;
import com.loopins.core.dto.response.OrderResponse;
import com.loopins.core.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders and order lifecycle")
public class OrderController {

    private final OrderService orderService;

    /**
     * Gets an order by ID.
     */
    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves an order with all its items by order ID"
    )
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @Parameter(description = "Order ID (e.g., ORDER-XXXX)") @PathVariable String orderId) {
        log.info("GET /orders/{}", orderId);
        OrderResponse order = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * Gets orders for a user with pagination.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        log.info("GET /orders/user/{}", userId);
        Page<OrderResponse> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * Confirms payment for an order (called by Fulfillment Service).
     * Protected by X-SERVICE-KEY header.
     */
    @PostMapping("/{orderId}/payment-confirmed")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmPayment(
            @PathVariable String orderId,
            @Valid @RequestBody PaymentConfirmationRequest request) {
        log.info("POST /orders/{}/payment-confirmed - ref: {}", orderId, request.getPaymentReference());
        OrderResponse order = orderService.confirmPayment(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Payment confirmed"));
    }

    /**
     * Marks payment as failed for an order (called by Fulfillment Service).
     * Protected by X-SERVICE-KEY header.
     */
    @PostMapping("/{orderId}/payment-failed")
    public ResponseEntity<ApiResponse<OrderResponse>> paymentFailed(
            @PathVariable String orderId,
            @Valid @RequestBody PaymentConfirmationRequest request) {
        log.info("POST /orders/{}/payment-failed - ref: {}", orderId, request.getPaymentReference());
        OrderResponse order = orderService.markPaymentFailed(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Payment failure recorded"));
    }

    /**
     * Cancels an order.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable String orderId) {
        log.info("POST /orders/{}/cancel", orderId);
        OrderResponse order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled"));
    }

    /**
     * Marks order as shipped (admin/seller action).
     */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(@PathVariable String orderId) {
        log.info("POST /orders/{}/ship", orderId);
        OrderResponse order = orderService.markAsShipped(orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order marked as shipped"));
    }

    /**
     * Marks order as completed (admin/seller action).
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(@PathVariable String orderId) {
        log.info("POST /orders/{}/complete", orderId);
        OrderResponse order = orderService.markAsCompleted(orderId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order completed"));
    }
}


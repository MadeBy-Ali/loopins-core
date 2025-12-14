package com.loopins.core.service;

import com.loopins.core.domain.entity.Order;
import com.loopins.core.domain.entity.PaymentCallbackLog;
import com.loopins.core.domain.enums.OrderStatus;
import com.loopins.core.dto.request.PaymentConfirmationRequest;
import com.loopins.core.dto.response.OrderResponse;
import com.loopins.core.exception.BusinessException;
import com.loopins.core.exception.DuplicateRequestException;
import com.loopins.core.exception.ResourceNotFoundException;
import com.loopins.core.mapper.OrderMapper;
import com.loopins.core.repository.OrderRepository;
import com.loopins.core.repository.PaymentCallbackLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentCallbackLogRepository callbackLogRepository;
    private final OrderMapper orderMapper;

    /**
     * Gets an order by ID.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        log.debug("Fetching order: {}", orderId);
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return orderMapper.toResponse(order);
    }

    /**
     * Gets orders for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId, pageable)
                .map(orderMapper::toResponse);
    }

    /**
     * Confirms payment for an order (called by Fulfillment Service via webhook).
     * Includes idempotency check to handle duplicate callbacks.
     */
    @Transactional
    public OrderResponse confirmPayment(String orderId, PaymentConfirmationRequest request) {
        log.info("Processing payment confirmation for order: {}, reference: {}",
                orderId, request.getCallbackReference());

        // Idempotency check - prevent duplicate processing
        if (callbackLogRepository.existsByCallbackReference(request.getCallbackReference())) {
            log.warn("Duplicate callback received: {}", request.getCallbackReference());
            throw new DuplicateRequestException(
                    "Payment callback already processed: " + request.getCallbackReference());
        }

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Validate order state
        if (!order.canBeConfirmed()) {
            throw new BusinessException(
                    "Order cannot be confirmed. Current status: " + order.getStatus());
        }

        // Verify payment reference matches
        if (!order.getPaymentReference().equals(request.getPaymentReference())) {
            throw new BusinessException("Payment reference mismatch");
        }

        // Mark order as paid
        order.markAsPaid();
        Order savedOrder = orderRepository.save(order);

        // Log the callback for idempotency
        saveCallbackLog(orderId, request, "PAYMENT_SUCCESS");

        log.info("Order {} marked as PAID", orderId);
        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Marks payment as failed for an order.
     */
    @Transactional
    public OrderResponse markPaymentFailed(String orderId, PaymentConfirmationRequest request) {
        log.info("Processing payment failure for order: {}", orderId);

        // Idempotency check
        if (callbackLogRepository.existsByCallbackReference(request.getCallbackReference())) {
            log.warn("Duplicate callback received: {}", request.getCallbackReference());
            throw new DuplicateRequestException(
                    "Payment callback already processed: " + request.getCallbackReference());
        }

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.isPaid()) {
            throw new BusinessException("Cannot mark a paid order as failed");
        }

        order.markAsPaymentFailed();
        Order savedOrder = orderRepository.save(order);

        // Log the callback
        saveCallbackLog(orderId, request, "PAYMENT_FAILED");

        log.info("Order {} marked as PAYMENT_FAILED", orderId);
        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Cancels an order.
     */
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.canBeCancelled()) {
            throw new BusinessException(
                    "Order cannot be cancelled. Current status: " + order.getStatus());
        }

        order.markAsCancelled();
        Order savedOrder = orderRepository.save(order);

        log.info("Order {} cancelled", orderId);
        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Marks order as shipped.
     */
    @Transactional
    public OrderResponse markAsShipped(String orderId) {
        log.info("Marking order as shipped: {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException(
                    "Only paid orders can be shipped. Current status: " + order.getStatus());
        }

        order.markAsShipped();
        Order savedOrder = orderRepository.save(order);

        log.info("Order {} marked as SHIPPED", orderId);
        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Marks order as completed.
     */
    @Transactional
    public OrderResponse markAsCompleted(String orderId) {
        log.info("Marking order as completed: {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException(
                    "Only shipped orders can be completed. Current status: " + order.getStatus());
        }

        order.markAsCompleted();
        Order savedOrder = orderRepository.save(order);

        log.info("Order {} marked as COMPLETED", orderId);
        return orderMapper.toResponse(savedOrder);
    }

    /**
     * Gets order entity for internal use.
     */
    @Transactional(readOnly = true)
    public Order getOrderEntity(String orderId) {
        return orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }

    private void saveCallbackLog(String orderId, PaymentConfirmationRequest request, String type) {
        PaymentCallbackLog callbackLog = PaymentCallbackLog.builder()
                .orderId(orderId)
                .callbackReference(request.getCallbackReference())
                .callbackType(type)
                .payload(request.getPayload())
                .build();
        callbackLogRepository.save(callbackLog);
    }
}


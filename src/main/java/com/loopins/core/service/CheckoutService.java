package com.loopins.core.service;

import com.loopins.core.client.FulfillmentClient;
import com.loopins.core.client.dto.*;
import com.loopins.core.domain.entity.Cart;
import com.loopins.core.domain.entity.Order;
import com.loopins.core.domain.entity.OrderItem;
import com.loopins.core.domain.entity.User;
import com.loopins.core.dto.request.CheckoutRequest;
import com.loopins.core.dto.response.CheckoutResponse;
import com.loopins.core.exception.BusinessException;
import com.loopins.core.exception.ExternalServiceException;
import com.loopins.core.exception.ResourceNotFoundException;
import com.loopins.core.repository.OrderRepository;
import com.loopins.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Checkout orchestration service.
 * Coordinates the checkout flow: Cart validation → Shipping quote → Order creation → Payment initiation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FulfillmentClient fulfillmentClient;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Performs checkout process:
     * 1. Validate cart
     * 2. Get shipping quote from Fulfillment Service
     * 3. Create order with calculated totals
     * 4. Initiate payment via Fulfillment Service
     * 5. Return payment URL
     */
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        log.info("Starting checkout for cart: {}, user: {}", request.getCartId(), request.getUserId());

        // 1. Validate cart and user
        Cart cart = cartService.getCartEntity(request.getCartId());
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        validateCart(cart, request.getUserId());

        // 2. Get shipping quote
        BigDecimal shippingFee = getShippingQuote(request);
        log.info("Shipping fee calculated: {}", shippingFee);

        // 3. Create order
        Order order = createOrderFromCart(cart, user, request.getShippingAddress(), shippingFee);
        log.info("Order created: {}", order.getId());

        // 4. Initiate payment
        PaymentInitiateResponse paymentResponse = initiatePayment(order, user);

        // 5. Update order with payment info
        if (paymentResponse.isSuccess()) {
            order.markAsPaymentPending(paymentResponse.getPaymentUrl(), paymentResponse.getPaymentReference());
            orderRepository.save(order);

            // Mark cart as checked out
            cartService.markCartAsCheckedOut(cart.getId());

            log.info("Checkout completed. Order: {}, Payment URL: {}",
                    order.getId(), paymentResponse.getPaymentUrl());

            return CheckoutResponse.builder()
                    .orderId(order.getId())
                    .status(order.getStatus().name())
                    .subtotal(order.getSubtotal())
                    .shippingFee(order.getShippingFee())
                    .totalAmount(order.getTotalAmount())
                    .paymentUrl(paymentResponse.getPaymentUrl())
                    .paymentReference(paymentResponse.getPaymentReference())
                    .message("Order created successfully. Please complete payment.")
                    .build();
        } else {
            // Payment initiation failed, but order is created
            // Keep order in CREATED status for retry
            order.markAsCreated();
            orderRepository.save(order);

            log.warn("Payment initiation failed for order: {}. Error: {}",
                    order.getId(), paymentResponse.getErrorMessage());

            return CheckoutResponse.builder()
                    .orderId(order.getId())
                    .status(order.getStatus().name())
                    .subtotal(order.getSubtotal())
                    .shippingFee(order.getShippingFee())
                    .totalAmount(order.getTotalAmount())
                    .message("Order created but payment initiation failed. Please retry payment.")
                    .build();
        }
    }

    /**
     * Retries payment initiation for an existing order.
     */
    @Transactional
    public CheckoutResponse retryPayment(String orderId) {
        log.info("Retrying payment for order: {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.isPaid()) {
            throw new BusinessException("Order is already paid");
        }

        User user = order.getUser();
        PaymentInitiateResponse paymentResponse = initiatePayment(order, user);

        if (paymentResponse.isSuccess()) {
            order.markAsPaymentPending(paymentResponse.getPaymentUrl(), paymentResponse.getPaymentReference());
            orderRepository.save(order);

            return CheckoutResponse.builder()
                    .orderId(order.getId())
                    .status(order.getStatus().name())
                    .subtotal(order.getSubtotal())
                    .shippingFee(order.getShippingFee())
                    .totalAmount(order.getTotalAmount())
                    .paymentUrl(paymentResponse.getPaymentUrl())
                    .paymentReference(paymentResponse.getPaymentReference())
                    .message("Payment initiated successfully.")
                    .build();
        } else {
            throw new ExternalServiceException("Payment",
                    "Failed to initiate payment: " + paymentResponse.getErrorMessage());
        }
    }

    private void validateCart(Cart cart, Long userId) {
        if (!cart.getUser().getId().equals(userId)) {
            throw new BusinessException("Cart does not belong to the specified user");
        }

        if (!cart.isActive()) {
            throw new BusinessException("Cart has already been checked out");
        }

        if (cart.isEmpty()) {
            throw new BusinessException("Cannot checkout an empty cart");
        }

        // Check if cart has already been converted to an order
        if (orderRepository.existsByCartId(cart.getId())) {
            throw new BusinessException("An order has already been created from this cart");
        }
    }

    private BigDecimal getShippingQuote(CheckoutRequest request) {
        if (request.getOriginCity() == null || request.getDestinationCity() == null) {
            // Default shipping fee if cities not provided
            log.warn("Origin/destination cities not provided, using default shipping fee");
            return new BigDecimal("15000"); // Default shipping fee
        }

        ShippingQuoteRequest shippingRequest = ShippingQuoteRequest.builder()
                .originCity(request.getOriginCity())
                .destinationCity(request.getDestinationCity())
                .weightInKg(request.getWeightInKg() != null ? request.getWeightInKg() : 1.0)
                .courierCode("jne")
                .build();

        ShippingQuoteResponse response = fulfillmentClient.getShippingQuote(shippingRequest);

        if (!response.isSuccess()) {
            log.error("Failed to get shipping quote: {}", response.getErrorMessage());
            throw new ExternalServiceException("Shipping",
                    "Failed to calculate shipping fee: " + response.getErrorMessage());
        }

        return response.getPrice();
    }

    private Order createOrderFromCart(Cart cart, User user, String shippingAddress, BigDecimal shippingFee) {
        Order order = Order.builder()
                .user(user)
                .cart(cart)
                .shippingAddress(shippingAddress)
                .shippingFee(shippingFee)
                .build();

        // Copy items from cart to order
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = OrderItem.fromCartItem(cartItem);
            order.addItem(orderItem);
        });

        // Calculate totals
        order.calculateTotals();

        return orderRepository.save(order);
    }

    private PaymentInitiateResponse initiatePayment(Order order, User user) {
        PaymentInitiateRequest paymentRequest = PaymentInitiateRequest.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .currency("IDR")
                .customerEmail(user.getEmail())
                .customerName(user.getUsername())
                .description("Payment for order " + order.getId())
                .items(order.getItems().stream()
                        .map(item -> PaymentInitiateRequest.PaymentItem.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .unitPrice(item.getUnitPrice())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .callbackUrl("http://localhost:" + serverPort + "/api/orders/" + order.getId() + "/payment-confirmed")
                .returnUrl("http://localhost:3000/orders/" + order.getId())
                .build();

        return fulfillmentClient.initiatePayment(paymentRequest);
    }
}


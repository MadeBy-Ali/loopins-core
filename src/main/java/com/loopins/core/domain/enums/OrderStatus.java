package com.loopins.core.domain.enums;

/**
 * Order status enum representing the order lifecycle.
 */
public enum OrderStatus {
    DRAFT,              // Order created but not submitted
    CREATED,            // Order submitted, awaiting payment initiation
    PAYMENT_PENDING,    // Payment initiated, waiting for confirmation
    PAID,               // Payment confirmed
    PAYMENT_FAILED,     // Payment failed
    CANCELLED,          // Order cancelled
    SHIPPED,            // Order shipped
    COMPLETED           // Order delivered/completed
}


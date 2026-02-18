package com.loopins.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Cart ID is required")
    private Long cartId;

    // Optional - can be null for guest checkout
    private Long userId;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    // For guest checkout (optional)
    private String guestEmail;
    private String guestName;
    private String guestPhone;

    // For shipping quote calculation
    private String originCity;
    private String destinationCity;
    private Double weightInKg;

    // Bypass flags for testing/development
    private Boolean bypassShipping; // If true, uses default shipping fee instead of calling fulfillment service
    private Boolean bypassPayment; // If true, skips payment initiation (for testing order creation only)
}


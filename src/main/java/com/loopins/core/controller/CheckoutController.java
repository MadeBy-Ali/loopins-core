package com.loopins.core.controller;

import com.loopins.core.dto.request.CheckoutRequest;
import com.loopins.core.dto.response.ApiResponse;
import com.loopins.core.dto.response.CheckoutResponse;
import com.loopins.core.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "APIs for cart checkout and order creation")
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * Performs checkout: validates cart, calculates shipping, creates order, initiates payment.
     */
    @PostMapping("/checkout")
    @Operation(
        summary = "Checkout cart",
        description = """
            Performs the complete checkout process:
            1. Validates cart and user
            2. Gets shipping quote from Fulfillment Service
            3. Creates order with calculated totals
            4. Initiates payment via Fulfillment Service
            5. Returns payment URL
            """
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Checkout completed successfully",
            content = @Content(schema = @Schema(implementation = CheckoutResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid cart or checkout data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "503",
            description = "External service (shipping/payment) unavailable"
        )
    })
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request) {
        log.info("POST /orders/checkout - cart: {}, user: {}",
                request.getCartId(), request.getUserId());
        CheckoutResponse response = checkoutService.checkout(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Checkout processed"));
    }

    /**
     * Retries payment initiation for an existing order.
     */
    @PostMapping("/{orderId}/retry-payment")
    @Operation(
        summary = "Retry payment",
        description = "Retries payment initiation for an existing order that failed payment"
    )
    public ResponseEntity<ApiResponse<CheckoutResponse>> retryPayment(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        log.info("POST /orders/{}/retry-payment", orderId);
        CheckoutResponse response = checkoutService.retryPayment(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment initiated"));
    }
}


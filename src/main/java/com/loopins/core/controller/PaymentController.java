package com.loopins.core.controller;

import com.loopins.core.dto.response.ApiResponse;
import com.loopins.core.dto.response.MidtransSnapResponse;
import com.loopins.core.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "APIs for payment processing with Midtrans")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create Midtrans Snap payment for an order
     */
    @PostMapping("/snap/{orderId}")
    @Operation(
        summary = "Create Midtrans Snap payment",
        description = "Creates a Midtrans Snap payment token for QRIS and other payment methods"
    )
    public ResponseEntity<ApiResponse<MidtransSnapResponse>> createSnapPayment(
            @Parameter(description = "Order ID") @PathVariable String orderId) {
        log.info("POST /payments/snap/{}", orderId);
        MidtransSnapResponse response = paymentService.createSnapPayment(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment token created"));
    }


    /**
     * Midtrans payment notification callback
     */
    @PostMapping("/callback")
    @Operation(
        summary = "Payment notification callback",
        description = "Receives payment status notifications from Midtrans"
    )
    public ResponseEntity<ApiResponse<String>> handlePaymentCallback(
            @RequestBody Map<String, Object> notification) {
        log.info("POST /payments/callback - Received Midtrans notification");
        paymentService.handlePaymentNotification(notification);
        return ResponseEntity.ok(ApiResponse.success("OK", "Notification processed"));
    }
}

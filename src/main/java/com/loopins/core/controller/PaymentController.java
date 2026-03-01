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
     * Midtrans Snap finish/unfinish/error redirect (browser redirect after payment)
     */
    @GetMapping("/callback/finish")
    @Operation(summary = "Snap finish redirect", description = "Browser redirect URL after Snap payment completion")
    public ResponseEntity<ApiResponse<String>> snapFinish(
            @RequestParam(required = false) String order_id,
            @RequestParam(required = false) String transaction_status) {
        log.info("GET /payments/callback/finish - order_id={}, status={}", order_id, transaction_status);
        return ResponseEntity.ok(ApiResponse.success(transaction_status, "Payment completed. Status: " + transaction_status));
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
        try {
            paymentService.handlePaymentNotification(notification);
        } catch (Exception e) {
            // Always return 200 to Midtrans regardless of internal errors
            // Midtrans will keep retrying if it receives non-200
            log.error("Error processing payment notification: {}", e.getMessage());
        }
        return ResponseEntity.ok(ApiResponse.success("OK", "Notification processed"));
    }
}

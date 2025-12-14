package com.loopins.core.client;

import com.loopins.core.client.config.FeignClientConfig;
import com.loopins.core.client.dto.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "fulfillment-service",
    url = "${fulfillment.service.url}",
    configuration = FeignClientConfig.class
)
public interface FulfillmentClient {

    @PostMapping("/api/shipping/quote")
    @CircuitBreaker(name = "fulfillment", fallbackMethod = "getShippingQuoteFallback")
    @Retry(name = "fulfillment")
    ShippingQuoteResponse getShippingQuote(@RequestBody ShippingQuoteRequest request);

    @PostMapping("/api/payments/initiate")
    @CircuitBreaker(name = "fulfillment", fallbackMethod = "initiatePaymentFallback")
    @Retry(name = "fulfillment")
    PaymentInitiateResponse initiatePayment(@RequestBody PaymentInitiateRequest request);

    @PostMapping("/api/notifications/email")
    @CircuitBreaker(name = "fulfillment", fallbackMethod = "sendEmailNotificationFallback")
    @Retry(name = "fulfillment")
    void sendEmailNotification(@RequestBody EmailNotificationRequest request);

    // Fallback methods
    default ShippingQuoteResponse getShippingQuoteFallback(ShippingQuoteRequest request, Throwable t) {
        return ShippingQuoteResponse.builder()
                .success(false)
                .errorMessage("Shipping service temporarily unavailable: " + t.getMessage())
                .build();
    }

    default PaymentInitiateResponse initiatePaymentFallback(PaymentInitiateRequest request, Throwable t) {
        return PaymentInitiateResponse.builder()
                .success(false)
                .errorMessage("Payment service temporarily unavailable: " + t.getMessage())
                .build();
    }

    default void sendEmailNotificationFallback(EmailNotificationRequest request, Throwable t) {
        // Log the failure but don't fail the main flow
        // Email notifications are not critical for order processing
    }
}


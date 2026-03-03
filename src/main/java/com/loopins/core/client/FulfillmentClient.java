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

    @PostMapping("/api/emails/order-success")
    @CircuitBreaker(name = "fulfillment-email", fallbackMethod = "sendEmailNotificationFallback")
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
        org.slf4j.LoggerFactory.getLogger(FulfillmentClient.class)
                .error("[EMAIL FALLBACK] Failed to send email notification for order: {}. " +
                        "Recipient: {}. Cause: {}", request.getOrderId(), request.getRecipientEmail(), t.getMessage(), t);
    }
}


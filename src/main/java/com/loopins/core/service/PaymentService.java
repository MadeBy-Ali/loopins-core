package com.loopins.core.service;

import com.loopins.core.domain.entity.Order;
import com.loopins.core.domain.entity.PaymentCallbackLog;
import com.loopins.core.dto.response.MidtransSnapResponse;
import com.loopins.core.exception.BusinessException;
import com.loopins.core.exception.DuplicateRequestException;
import com.loopins.core.exception.ResourceNotFoundException;
import com.loopins.core.repository.OrderRepository;
import com.loopins.core.event.OrderPaidEvent;
import com.loopins.core.repository.PaymentCallbackLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Payment orchestration service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MidtransPaymentService midtransPaymentService;
    private final OrderRepository orderRepository;
    private final PaymentCallbackLogRepository paymentCallbackLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create Midtrans Snap payment for an order
     */
    @Transactional
    public MidtransSnapResponse createSnapPayment(String orderId) {
        log.info("Creating Snap payment for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Validate order can receive payment
        if (!order.canInitiatePayment()) {
            throw new BusinessException("Order " + orderId + " is not in a valid state for payment. Current status: " + order.getStatus());
        }

        // Create Midtrans Snap transaction
        Map<String, String> snapResult = midtransPaymentService.createSnapTransaction(order);

        // Update order with payment info
        order.markAsPaymentPending(snapResult.get("redirect_url"), snapResult.get("token"));
        orderRepository.save(order);

        log.info("Snap payment created for order: {}, token: {}", orderId, snapResult.get("token"));

        return MidtransSnapResponse.builder()
                .token(snapResult.get("token"))
                .redirectUrl(snapResult.get("redirect_url"))
                .orderId(orderId)
                .grossAmount(order.getTotalAmount().longValue())
                .message("Payment token created successfully. Please complete payment.")
                .build();
    }


    /**
     * Handle payment notification from Midtrans
     */
    @Transactional
    public void handlePaymentNotification(Map<String, Object> notification) {
        String orderId = (String) notification.get("order_id");
        String transactionStatus = (String) notification.get("transaction_status");
        String fraudStatus = (String) notification.get("fraud_status");
        String transactionId = (String) notification.get("transaction_id");

        log.info("Processing payment notification for order: {}, status: {}, fraud: {}",
                orderId, transactionStatus, fraudStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Check for duplicate notification (keyed by transactionId + status to allow
        // pending → settlement transitions for the same transaction)
        String callbackReference = transactionId + "_" + transactionStatus;
        if (paymentCallbackLogRepository.existsByCallbackReference(callbackReference)) {
            log.warn("Duplicate payment notification received for transaction: {}, status: {}", transactionId, transactionStatus);
            throw new DuplicateRequestException("Payment notification already processed");
        }

        // Log the callback
        PaymentCallbackLog callbackLog = new PaymentCallbackLog();
        callbackLog.setOrderId(orderId);
        callbackLog.setCallbackReference(callbackReference);
        callbackLog.setCallbackType("MIDTRANS_NOTIFICATION");
        callbackLog.setPayload(notification.toString());
        paymentCallbackLogRepository.save(callbackLog);

        // Process based on transaction status
        switch (transactionStatus) {
            case "capture":
                if ("accept".equals(fraudStatus)) {
                    order.markAsPaid();
                    log.info("Order {} marked as PAID", orderId);
                }
                break;
            case "settlement":
                order.markAsPaid();
                log.info("Order {} marked as PAID", orderId);
                break;
            case "pending":
                order.markAsPaymentPending(order.getPaymentUrl(), order.getPaymentReference());
                log.info("Order {} is pending payment", orderId);
                break;
            case "deny":
            case "expire":
            case "cancel":
                order.markAsCancelled();
                log.info("Order {} cancelled due to payment status: {}", orderId, transactionStatus);
                break;
            default:
                log.warn("Unknown transaction status: {} for order: {}", transactionStatus, orderId);
        }

        orderRepository.save(order);

        // Publish event after save — listener fires after transaction commits
        if ("settlement".equals(transactionStatus) ||
                ("capture".equals(transactionStatus) && "accept".equals(fraudStatus))) {
            eventPublisher.publishEvent(new OrderPaidEvent(this, order));
        }
    }
}

package com.loopins.core.event;

import com.loopins.core.client.FulfillmentClient;
import com.loopins.core.client.dto.EmailNotificationRequest;
import com.loopins.core.domain.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {

    private final FulfillmentClient fulfillmentClient;

    @Value("${admin.notification.email:admin@loopins.com}")
    private String adminEmail;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        Order order = event.getOrder();

        // 1. Notify admin
        log.info("Sending admin notification for paid order: {}", order.getId());
        fulfillmentClient.sendEmailNotification(EmailNotificationRequest.builder()
                .orderId(order.getId())
                .recipientEmail(adminEmail)
                .customerName(order.getCustomerName())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .build());
        log.info("Admin notification sent for order: {}", order.getId());

        // 2. Notify customer (if email is available)
        String customerEmail = order.getCustomerEmail();
        if (customerEmail != null && !customerEmail.isBlank()) {
            log.info("Sending customer payment confirmation for order: {}", order.getId());
            fulfillmentClient.sendEmailNotification(EmailNotificationRequest.builder()
                    .orderId(order.getId())
                    .recipientEmail(customerEmail)
                    .customerName(order.getCustomerName())
                    .totalAmount(order.getTotalAmount())
                    .shippingAddress(order.getShippingAddress())
                    .build());
            log.info("Customer confirmation sent to: {}", customerEmail);
        }
    }
}

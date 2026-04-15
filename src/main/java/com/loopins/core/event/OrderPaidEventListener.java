package com.loopins.core.event;

import com.loopins.core.client.FulfillmentClient;
import com.loopins.core.client.dto.EmailNotificationRequest;
import com.loopins.core.domain.entity.Order;
import com.loopins.core.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {

    private final FulfillmentClient fulfillmentClient;
    private final OrderRepository orderRepository;

    @Value("${admin.notification.emails:support@loopinsstudio.com,loopins.std@gmail.com}")
    private String adminEmailsConfig;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onOrderPaid(OrderPaidEvent event) {
        String orderId = event.getOrder().getId();
        log.info("OrderPaidEvent received for order: {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseGet(() -> event.getOrder());

        List<EmailNotificationRequest.OrderItemDetail> itemDetails = order.getItems().stream()
                .map(item -> EmailNotificationRequest.OrderItemDetail.builder()
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        log.info("Order {} has {} item(s) to include in email", orderId, itemDetails.size());
        itemDetails.forEach(i -> log.debug("  - Item: {} x{} @ {}", i.getProductName(), i.getQuantity(), i.getUnitPrice()));

        List<String> adminEmails = Arrays.stream(adminEmailsConfig.split(","))
                .map(String::trim)
                .filter(e -> !e.isBlank())
                .collect(Collectors.toList());

        // 1. Notify each admin email
        for (String adminEmail : adminEmails) {
            log.info("Sending admin notification to {} for paid order: {}", adminEmail, orderId);
            try {
                fulfillmentClient.sendEmailNotification(EmailNotificationRequest.builder()
                        .orderId(orderId)
                        .recipientEmail(adminEmail)
                        .customerName(order.getCustomerName())
                        .totalAmount(order.getTotalAmount())
                        .shippingAddress(order.getShippingAddress())
                        .items(itemDetails)
                        .build());
                log.info("Admin notification dispatched to {} for order: {}", adminEmail, orderId);
            } catch (Exception e) {
                log.error("Failed to dispatch admin notification to {} for order {}: {}",
                        adminEmail, orderId, e.getMessage(), e);
            }
        }

        // 2. Notify customer (if email is available)
        String customerEmail = order.getCustomerEmail();
        if (customerEmail != null && !customerEmail.isBlank()) {
            log.info("Sending customer payment confirmation to {} for order: {}", customerEmail, orderId);
            try {
                fulfillmentClient.sendEmailNotification(EmailNotificationRequest.builder()
                        .orderId(orderId)
                        .recipientEmail(customerEmail)
                        .customerName(order.getCustomerName())
                        .totalAmount(order.getTotalAmount())
                        .shippingAddress(order.getShippingAddress())
                        .items(itemDetails)
                        .build());
                log.info("Customer confirmation dispatched to: {} for order: {}", customerEmail, orderId);
            } catch (Exception e) {
                log.error("Failed to dispatch customer notification to {} for order {}: {}",
                        customerEmail, orderId, e.getMessage(), e);
            }
        } else {
            log.warn("No customer email found for order: {}, skipping customer notification", orderId);
        }
    }
}

package com.loopins.core.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRequest {

    private String orderId;
    private String recipientEmail;
    private String customerName;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemDetail> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}

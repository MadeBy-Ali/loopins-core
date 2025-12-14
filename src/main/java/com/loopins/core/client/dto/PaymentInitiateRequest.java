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
public class PaymentInitiateRequest {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String customerEmail;
    private String customerName;
    private String description;
    private List<PaymentItem> items;
    private String callbackUrl;
    private String returnUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentItem {
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
    }
}


package com.loopins.core.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
}


package com.loopins.core.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRequest {

    private String orderId;
    private String recipientEmail;
    private String recipientName;
    private String subject;
    private String templateType; // ORDER_CREATED, PAYMENT_SUCCESS, ORDER_SHIPPED, etc.
    private Object templateData;
}


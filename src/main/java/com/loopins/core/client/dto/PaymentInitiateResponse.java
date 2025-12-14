package com.loopins.core.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateResponse {

    private boolean success;
    private String paymentReference;
    private String paymentUrl;
    private String status;
    private String errorMessage;
}


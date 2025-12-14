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
public class ShippingQuoteResponse {

    private String courierCode;
    private String courierName;
    private String serviceType;
    private BigDecimal price;
    private String estimatedDelivery;
    private boolean success;
    private String errorMessage;
}


package com.loopins.core.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingQuoteRequest {

    private String originCity;
    private String destinationCity;
    private Double weightInKg;
    private String courierCode; // e.g., "jne", "jnt", "sicepat"
}


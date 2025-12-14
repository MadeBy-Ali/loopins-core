package com.loopins.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmationRequest {

    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

    @NotBlank(message = "Callback reference is required")
    private String callbackReference;

    private String status;
    private String payload;
}


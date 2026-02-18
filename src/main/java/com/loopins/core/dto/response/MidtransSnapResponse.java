package com.loopins.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Midtrans Snap payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MidtransSnapResponse {

    /**
     * Snap token for frontend integration
     */
    private String token;

    /**
     * Redirect URL for payment page
     */
    private String redirectUrl;

    /**
     * Order ID
     */
    private String orderId;

    /**
     * Total amount to be paid
     */
    private Long grossAmount;

    /**
     * Message for user
     */
    private String message;
}

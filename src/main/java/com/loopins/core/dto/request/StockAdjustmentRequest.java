package com.loopins.core.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity; // positive = add stock, negative = reduce stock

    private String reason; // optional note e.g. "restock", "damaged", "manual correction"
}

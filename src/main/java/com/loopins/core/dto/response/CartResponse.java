package com.loopins.core.dto.response;

import com.loopins.core.domain.enums.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private Long id;
    private Long userId;
    private CartStatus status;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private int totalItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


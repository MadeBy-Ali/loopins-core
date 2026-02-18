package com.loopins.core.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCartRequest {

    private Long userId;

    private String sessionId;

    @AssertTrue(message = "Either userId or sessionId must be provided")
    private boolean isValid() {
        return (userId != null && sessionId == null) || (userId == null && sessionId != null);
    }
}

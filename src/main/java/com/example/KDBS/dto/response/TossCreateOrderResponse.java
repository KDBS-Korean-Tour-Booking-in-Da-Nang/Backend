package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossCreateOrderResponse {
    private boolean success;
    private String clientKey;
    private String customerKey;
    private BigDecimal amount;  // tổng tiền booking
    private String orderId;
    private String successUrl;
    private String failUrl;
    private String message;     // optional
}

package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumPaymentResponse {
    
    private String orderId;
    private String transactionId;
    private BigDecimal amount;
    private String paymentUrl;
    private String orderInfo;
    private LocalDateTime validUntil;
    private String message;
    private boolean success;
}

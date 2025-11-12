package com.example.KDBS.dto.response;

import com.example.KDBS.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossConfirmResponse {
    private boolean success;

    // Echo từ Toss / request
    private String orderId;
    private BigDecimal amount;

    // Kết quả hệ thống
    private TransactionStatus transactionStatus; // SUCCESS/FAILED/PENDING
    private String orderInfo;         // PURCHASED/PENDING
    private String payType;                      // "TOSS"
    private String responseTime;
}

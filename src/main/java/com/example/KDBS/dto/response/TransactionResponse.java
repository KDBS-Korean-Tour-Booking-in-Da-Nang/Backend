package com.example.KDBS.dto.response;

import com.example.KDBS.enums.PaymentMethod;
import com.example.KDBS.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String orderId;
    private BigDecimal amount;
    private TransactionStatus status;
    private PaymentMethod paymentMethod;
    private String orderInfo;
    private String username;
    private String email;
    private String avatar;
    private LocalDateTime createdTime;
}

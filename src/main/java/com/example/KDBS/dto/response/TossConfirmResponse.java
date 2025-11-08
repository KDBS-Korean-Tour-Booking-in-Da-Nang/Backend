package com.example.KDBS.dto.response;

import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossConfirmResponse {
    private boolean success;

    // Echo từ Toss / request
    private String orderId;
    private String paymentKey;
    private BigDecimal amount;

    // Kết quả hệ thống
    private TransactionStatus transactionStatus; // SUCCESS/FAILED/PENDING
    private BookingStatus bookingStatus;         // PURCHASED/PENDING
    private String payType;                      // "TOSS"
    private String responseTime;

    // Nếu lỗi
    private String code;     // ví dụ INVALID_API_KEY, ALREADY_PROCESSED...
    private String message;  // mô tả ngắn gọn
}

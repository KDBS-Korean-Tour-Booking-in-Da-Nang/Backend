package com.example.KDBS.dto.response;

import com.example.KDBS.enums.VoucherDiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyVoucherResponse {
    private Long voucherId;
    private String voucherCode;
    private VoucherDiscountType discountType;  // PERCENTAGE or FIXED
    private BigDecimal discountValue;          // 20% or 250,000₫

    private BigDecimal originalTotal;
    private BigDecimal discountAmount;         // total discount applied
    private BigDecimal finalTotal;

    private BigDecimal depositPercentage;      // e.g. 0.5 (50%)
    private boolean oneTimePayment;            // depositPercentage == 1.0

    // After discount:
    private BigDecimal finalDepositAmount;
    private BigDecimal finalRemainingAmount;

    // Only for percentage vouchers:
    private BigDecimal depositDiscountAmount;
    private BigDecimal remainingDiscountAmount;
}
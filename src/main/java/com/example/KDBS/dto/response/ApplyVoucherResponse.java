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
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
}



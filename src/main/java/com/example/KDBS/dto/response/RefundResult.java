package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RefundResult {
    private int refundPercentage;
    private BigDecimal refundAmount;
}

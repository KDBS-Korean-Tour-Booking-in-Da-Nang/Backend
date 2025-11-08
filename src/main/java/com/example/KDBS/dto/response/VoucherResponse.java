package com.example.KDBS.dto.response;

import com.example.KDBS.enums.VoucherDiscountType;
import com.example.KDBS.enums.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponse {
    private Long voucherId;
    private Integer companyId;
    private String code;
    private String name;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoucherStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



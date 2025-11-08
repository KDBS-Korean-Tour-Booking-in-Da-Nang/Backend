package com.example.KDBS.dto.request;

import com.example.KDBS.enums.VoucherDiscountType;
import com.example.KDBS.enums.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherCreateRequest {
    private Integer companyId;
    private String code;
    private String name;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private Integer totalQuantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoucherStatus status;
    private List<Long> tourIds; // null or empty -> apply to all tours in company
}



package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.VoucherCreateRequest;
import com.example.KDBS.dto.response.ApplyVoucherResponse;
import com.example.KDBS.dto.response.VoucherResponse;
import com.example.KDBS.model.Voucher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface VoucherMapper {

    @Mapping(target = "voucherId", ignore = true)
    @Mapping(target = "remainingQuantity", ignore = true) // Set in service: remainingQuantity = totalQuantity
    @Mapping(target = "status", ignore = true) // Set in service: default ACTIVE if null
    @Mapping(target = "createdAt", ignore = true) // Handled by @PrePersist
    @Mapping(target = "updatedAt", ignore = true) // Handled by @PrePersist
    Voucher toVoucher(VoucherCreateRequest request);


    VoucherResponse toVoucherResponse(Voucher voucher);

    @Mapping(target = "voucherId", source = "voucher.voucherId")
    @Mapping(target = "voucherCode", source = "voucher.code")
    @Mapping(target = "originalTotal", ignore = true) // Calculated in service
    @Mapping(target = "discountAmount", ignore = true) // Calculated in service
    @Mapping(target = "finalTotal", ignore = true) // Calculated in service
    ApplyVoucherResponse toApplyVoucherResponse(Voucher voucher);
}


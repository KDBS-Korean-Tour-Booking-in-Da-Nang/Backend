package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ApplyVoucherRequest;
import com.example.KDBS.dto.request.VoucherCreateRequest;
import com.example.KDBS.dto.response.ApplyVoucherResponse;
import com.example.KDBS.dto.response.VoucherResponse;
import com.example.KDBS.model.Voucher;
import com.example.KDBS.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<VoucherResponse> create(@Valid @RequestBody VoucherCreateRequest request) {
        return ResponseEntity.ok(voucherService.createVoucher(request));
    }

    @GetMapping("/preview-all/{bookingId}")
    public ResponseEntity<List<ApplyVoucherResponse>> previewAllAvailableVouchers(@PathVariable Long bookingId) {
        return ResponseEntity.ok(voucherService.previewAllAvailableVouchers(bookingId));
    }

    @PostMapping("/preview-apply")
    public ResponseEntity<ApplyVoucherResponse> apply(@Valid @RequestBody ApplyVoucherRequest request) {
        var preview = voucherService.previewApplyVoucher(request);
        return ResponseEntity.ok(preview);
    }

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/{tourId}")
    public ResponseEntity<List<Voucher>> getVouchersByTourId(@PathVariable Long tourId) {
        return ResponseEntity.ok(voucherService.getAllVoucherByTourId(tourId));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<VoucherResponse>> getVouchersByCompanyId(@PathVariable Integer companyId) {
        return ResponseEntity.ok(voucherService.getVouchersByCompanyId(companyId));
    }
}

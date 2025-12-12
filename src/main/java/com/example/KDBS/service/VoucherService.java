package com.example.KDBS.service;

import com.example.KDBS.dto.request.AllVoucherRequest;
import com.example.KDBS.dto.request.ApplyVoucherRequest;
import com.example.KDBS.dto.request.VoucherCreateRequest;
import com.example.KDBS.dto.response.ApplyVoucherResponse;
import com.example.KDBS.dto.response.VoucherResponse;
import com.example.KDBS.enums.VoucherDiscountType;
import com.example.KDBS.enums.VoucherStatus;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.VoucherMapper;
import com.example.KDBS.model.*;
import com.example.KDBS.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherTourMappingRepository voucherTourMappingRepository;
    private final VoucherUsageHistoryRepository voucherUsageHistoryRepository;
    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;

    @Transactional
    public VoucherResponse createVoucher(VoucherCreateRequest request) {
        // Check for duplicate voucher code
        if (voucherRepository.findByCompanyIdAndCode(request.getCompanyId(), request.getCode()).isPresent()) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_EXISTED);
        }

        // Map request to entity using mapper
        Voucher voucher = voucherMapper.toVoucher(request);
        
        // Set remainingQuantity = totalQuantity (initial state)
        voucher.setRemainingQuantity(request.getTotalQuantity());
        
        // Set default status if not provided
        if (request.getStatus() == null) {
            voucher.setStatus(VoucherStatus.ACTIVE);
        }

        Voucher savedVoucher = voucherRepository.save(voucher);

        // Create tour mappings if provided
        if (request.getTourIds() != null && !request.getTourIds().isEmpty()) {
            List<Tour> tours = tourRepository.findAllById(request.getTourIds());
            List<VoucherTourMapping> mappings = tours.stream()
                    .map(tour -> VoucherTourMapping.builder()
                            .voucher(savedVoucher)
                            .tour(tour)
                            .build())
                    .collect(Collectors.toList());
            voucherTourMappingRepository.saveAll(mappings);
        }

        return mapToVoucherResponseWithTourIds(savedVoucher);
    }

    @Transactional(readOnly = true)
    public ApplyVoucherResponse previewApplyVoucher(ApplyVoucherRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Voucher voucher = voucherRepository.findByCompanyIdAndCode(
                        booking.getTour().getCompanyId(),
                        request.getVoucherCode())
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        BigDecimal original = booking.getTotalAmount();

        if (isVoucherUsableForBooking(voucher, booking.getTour().getTourId(), original)) {
            throw new AppException(ErrorCode.VOUCHER_INVALID);
        }

        return buildVoucherPreview(voucher, booking.getTour(), original);
    }


    @Transactional
    public void attachVoucherToBookingPending(Long bookingId, ApplyVoucherResponse preview) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        booking.setVoucherId(preview.getVoucherId());
        booking.setVoucherCode(preview.getVoucherCode());
        booking.setVoucherDiscountApplied(preview.getDiscountAmount());
        lockVoucherOnPaymentSuccess(bookingId);
        bookingRepository.save(booking);
    }

    @Transactional
    public void lockVoucherOnPaymentSuccess(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (booking.getVoucherId() == null || Boolean.TRUE.equals(booking.getVoucherLocked())) {
            return; // nothing to lock or already locked
        }
        Voucher voucher = voucherRepository.findByIdForUpdate(booking.getVoucherId())
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (voucher.getRemainingQuantity() == null || voucher.getRemainingQuantity() <= 0) {
            throw new AppException(ErrorCode.VOUCHER_INVALID);
        }
        voucher.setRemainingQuantity(voucher.getRemainingQuantity() - 1);
        voucherRepository.save(voucher);

        User user = booking.getUserEmail() != null ? userRepository.findByEmail(booking.getUserEmail()).orElse(null) : null;

        VoucherUsageHistory history = VoucherUsageHistory.builder()
                .voucher(voucher)
                .booking(booking)
                .user(user)
                .discountAppliedAmount(booking.getVoucherDiscountApplied() != null ? booking.getVoucherDiscountApplied() : BigDecimal.ZERO)
                .canceled(false)
                .build();
        voucherUsageHistoryRepository.save(history);

        booking.setVoucherLocked(Boolean.TRUE);
        bookingRepository.save(booking);
    }

    @Transactional
    public void unlockVoucherOnBookingCancelled(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (booking.getVoucherId() == null || !Boolean.TRUE.equals(booking.getVoucherLocked())) {
            return;
        }
        Voucher voucher = voucherRepository.findByIdForUpdate(booking.getVoucherId())
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        voucher.setRemainingQuantity(voucher.getRemainingQuantity() + 1);
        voucherRepository.save(voucher);

        Optional<VoucherUsageHistory> last = voucherUsageHistoryRepository.findTopByBooking_BookingIdOrderByUsedAtDesc(bookingId);
        last.ifPresent(h -> {
            h.setCanceled(true);
            voucherUsageHistoryRepository.save(h);
        });

        booking.setVoucherLocked(Boolean.FALSE);
        bookingRepository.save(booking);
    }

    private BigDecimal calculateDiscountAmount(Voucher voucher, BigDecimal original) {
        BigDecimal value = voucher.getDiscountValue();
        if (value == null) return BigDecimal.ZERO;

        if (voucher.getDiscountType() == VoucherDiscountType.PERCENT) {
            // percent is whole number like 10, 20, 50
            return original
                    .multiply(value)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP) // SAFE DIV
                    .min(original); // discount cannot exceed original
        }

        // FIXED VALUE DISCOUNT
        return value.min(original); // cannot exceed price
    }


    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        if (vouchers.isEmpty()) {
            return new ArrayList<>();
        }
        return vouchers.stream()
                .map(this::mapToVoucherResponseWithTourIds)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Voucher> getAllVoucherByTourId(Long tourId) {
        List<Voucher> vouchers = voucherRepository.findAllVoucherByTourId(tourId);
        if (vouchers == null || vouchers.isEmpty()) {
            return new ArrayList<>();
        }
        return vouchers;
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getVouchersByCompanyId(Integer companyId) {
        if (companyId == null) {
            return new ArrayList<>();
        }
        List<Voucher> vouchers = voucherRepository.findByCompanyId(companyId);
        if (vouchers == null || vouchers.isEmpty()) {
            return new ArrayList<>();
        }
        return vouchers.stream()
                .map(this::mapToVoucherResponseWithTourIds)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplyVoucherResponse> previewAllAvailableVouchers(AllVoucherRequest request) {

        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        List<Voucher> vouchers = voucherRepository.findByCompanyId(tour.getCompanyId());
        if (vouchers == null || vouchers.isEmpty()) {
            return new ArrayList<>();
        }

        BigDecimal original = calculateBookingOriginalTotal(tour, request);
        List<ApplyVoucherResponse> available = new ArrayList<>();

        for (Voucher voucher : vouchers) {

            if (isVoucherUsableForBooking(voucher, tour.getTourId(), original)) {
                continue;
            }

            ApplyVoucherResponse resp = buildVoucherPreview(voucher, tour, original);

            available.add(resp);
        }

        return available;
    }

    private ApplyVoucherResponse buildVoucherPreview(Voucher voucher, Tour tour, BigDecimal original) {
        BigDecimal discount = calculateDiscountAmount(voucher, original);
        BigDecimal finalTotal = original.subtract(discount).max(BigDecimal.ZERO);

        ApplyVoucherResponse resp = voucherMapper.toApplyVoucherResponse(voucher);
        resp.setOriginalTotal(original);
        resp.setDiscountAmount(discount);
        resp.setFinalTotal(finalTotal);

        // add deposit split
        applyDepositSplit(resp, tour);

        return resp;
    }

    private BigDecimal calculateBookingOriginalTotal(Tour tour, AllVoucherRequest request) {
        BigDecimal adultTotal = tour.getAdultPrice().multiply(BigDecimal.valueOf(request.getAdultsCount()));
        BigDecimal childrenTotal = tour.getChildrenPrice().multiply(BigDecimal.valueOf(request.getChildrenCount()));
        BigDecimal babyTotal = tour.getBabyPrice().multiply(BigDecimal.valueOf(request.getBabiesCount()));
        return adultTotal.add(childrenTotal).add(babyTotal);
    }

    private void applyDepositSplit(ApplyVoucherResponse response, Tour tour) {
        BigDecimal dpPercent = BigDecimal.valueOf(tour.getDepositPercentage()); // e.g. 50
        BigDecimal dp = dpPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        response.setDepositPercentage(dp);
        boolean oneTime = dp.compareTo(BigDecimal.ONE) == 0;
        response.setOneTimePayment(oneTime);

        BigDecimal finalTotal = response.getFinalTotal();

        if (oneTime) {
            response.setFinalDepositAmount(finalTotal);
            response.setFinalRemainingAmount(BigDecimal.ZERO);

            response.setDepositDiscountAmount(response.getDiscountAmount()); // all discount belongs to one payment
            response.setRemainingDiscountAmount(BigDecimal.ZERO);
            return;
        }

        // Split normally
        BigDecimal finalDeposit = finalTotal.multiply(dp);
        BigDecimal finalRemaining = finalTotal.subtract(finalDeposit);

        response.setFinalDepositAmount(finalDeposit);
        response.setFinalRemainingAmount(finalRemaining);

        // For percentage voucher: proportional discount
        if (response.getDiscountType() == VoucherDiscountType.PERCENT) {
            BigDecimal discount = response.getDiscountAmount();
            BigDecimal depositDiscount = discount.multiply(dp);
            BigDecimal remainingDiscount = discount.subtract(depositDiscount);

            response.setDepositDiscountAmount(depositDiscount);
            response.setRemainingDiscountAmount(remainingDiscount);
        } else {
            // Fixed voucher → DO NOT split discount
            response.setDepositDiscountAmount(null);
            response.setRemainingDiscountAmount(null);
        }
    }


    /**
     * Check if voucher is usable for booking without throwing exception
     * Returns true if voucher can be applied, false otherwise
     */
    private boolean isVoucherUsableForBooking(Voucher voucher, Long tourId, BigDecimal originalTotal) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check status
        if (voucher.getStatus() == VoucherStatus.INACTIVE) {
            return true;
        }
        
        // Check date range
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return true;
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            return true;
        }
        
        // Check remaining quantity
        if (voucher.getRemainingQuantity() == null || voucher.getRemainingQuantity() <= 0) {
            return true;
        }
        
        // Check tour mapping - if voucher has specific tour mappings, tour must be in the list
        List<VoucherTourMapping> mappings = voucherTourMappingRepository.findByVoucher_VoucherId(voucher.getVoucherId());
        if (mappings != null && !mappings.isEmpty()) {
            boolean allowed = mappings.stream().anyMatch(m -> m.getTour().getTourId().equals(tourId));
            if (!allowed) {
                return true;
            }
        }
        
        // Check min order value
        return voucher.getMinOrderValue() != null && originalTotal.compareTo(voucher.getMinOrderValue()) < 0;
    }

    private VoucherResponse mapToVoucherResponseWithTourIds(Voucher voucher) {
        VoucherResponse response = voucherMapper.toVoucherResponse(voucher);

        List<Long> tourIds = voucherTourMappingRepository.findTourIdsByVoucherId(voucher.getVoucherId());
        if (tourIds != null && !tourIds.isEmpty()) {
            response.setTourIds(tourIds);
        } else {
            response.setTourIds(null);
        }
        
        return response;
    }

}




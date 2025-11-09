package com.example.KDBS.service;

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
        List<ApplyVoucherResponse> availableVouchers = previewAllAvailableVouchers(request.getBookingId());
        
        ApplyVoucherResponse found = availableVouchers.stream()
                .filter(v -> v.getVoucherCode().equals(request.getVoucherCode()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        
        return found;
    }

    @Transactional
    public void attachVoucherToBookingPending(Long bookingId, ApplyVoucherResponse preview) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        booking.setVoucherId(preview.getVoucherId());
        booking.setVoucherCode(preview.getVoucherCode());
        booking.setVoucherDiscountApplied(preview.getDiscountAmount());
        booking.setVoucherLocked(Boolean.FALSE);
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

    private BigDecimal calculateBookingOriginalTotal(Tour tour, Booking booking) {
        BigDecimal adultTotal = tour.getAdultPrice().multiply(BigDecimal.valueOf(booking.getAdultsCount()));
        BigDecimal childrenTotal = tour.getChildrenPrice().multiply(BigDecimal.valueOf(booking.getChildrenCount()));
        BigDecimal babyTotal = tour.getBabyPrice().multiply(BigDecimal.valueOf(booking.getBabiesCount()));
        return adultTotal.add(childrenTotal).add(babyTotal);
    }

    private BigDecimal calculateDiscountAmount(Voucher voucher, BigDecimal original) {
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENT) {
            BigDecimal percent = voucher.getDiscountValue();
            if (percent == null) return BigDecimal.ZERO;
            return original.multiply(percent).divide(BigDecimal.valueOf(100));
        }
        return voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        if (vouchers == null || vouchers.isEmpty()) {
            return new ArrayList<>();
        }
        return vouchers.stream()
                .map(this::mapToVoucherResponseWithTourIds)
                .collect(Collectors.toList());
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
    public List<ApplyVoucherResponse> previewAllAvailableVouchers(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        Tour tour = tourRepository.findById(booking.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        List<Voucher> vouchers = voucherRepository.findByCompanyId(tour.getCompanyId());
        if (vouchers == null || vouchers.isEmpty()) {
            return new ArrayList<>();
        }

        BigDecimal original = calculateBookingOriginalTotal(tour, booking);
        List<ApplyVoucherResponse> availableVouchers = new ArrayList<>();

        for (Voucher voucher : vouchers) {
            // Check if voucher is usable (without throwing exception)
            if (!isVoucherUsableForBooking(voucher, tour.getTourId(), booking, original)) {
                continue; // Skip this voucher if not usable
            }

            // Calculate discount and final total
            BigDecimal discount = calculateDiscountAmount(voucher, original);
            BigDecimal finalTotal = original.subtract(discount).max(BigDecimal.ZERO);

            ApplyVoucherResponse response = voucherMapper.toApplyVoucherResponse(voucher);
            response.setOriginalTotal(original);
            response.setDiscountAmount(discount);
            response.setFinalTotal(finalTotal);

            availableVouchers.add(response);
        }

        return availableVouchers;
    }

    /**
     * Check if voucher is usable for booking without throwing exception
     * Returns true if voucher can be applied, false otherwise
     */
    private boolean isVoucherUsableForBooking(Voucher voucher, Long tourId, Booking booking, BigDecimal originalTotal) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check status
        if (voucher.getStatus() == VoucherStatus.INACTIVE) {
            return false;
        }
        
        // Check date range
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return false;
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            return false;
        }
        
        // Check remaining quantity
        if (voucher.getRemainingQuantity() == null || voucher.getRemainingQuantity() <= 0) {
            return false;
        }
        
        // Check tour mapping - if voucher has specific tour mappings, tour must be in the list
        List<VoucherTourMapping> mappings = voucherTourMappingRepository.findByVoucher_VoucherId(voucher.getVoucherId());
        if (mappings != null && !mappings.isEmpty()) {
            boolean allowed = mappings.stream().anyMatch(m -> m.getTour().getTourId().equals(tourId));
            if (!allowed) {
                return false;
            }
        }
        
        // Check min order value
        if (voucher.getMinOrderValue() != null && originalTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return false;
        }
        
        return true;
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




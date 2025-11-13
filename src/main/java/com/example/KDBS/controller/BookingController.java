package com.example.KDBS.controller;

import com.example.KDBS.dto.request.BookingPaymentRequest;
import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.request.TossCreateOrderRequest;
import com.example.KDBS.dto.response.*;
import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.enums.InsuranceStatus;
import com.example.KDBS.service.BookingService;
import com.example.KDBS.service.TossPaymentService;
import com.example.KDBS.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final TossPaymentService tossPaymentService;
    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable long bookingId,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.updateBooking(bookingId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-status/{bookingId}")
    public ResponseEntity<BookingResponse> changeBookingStatus(@PathVariable long bookingId,
            @RequestParam BookingStatus status) {
        BookingResponse response = bookingService.changeBookingStatus(bookingId, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/booking-guest/insurance/change-status/{guestId}")
    public ResponseEntity<BookingGuestResponse> changeBookingGuestInsuranceStatus(@PathVariable long guestId,
            @RequestParam InsuranceStatus status) {
        BookingGuestResponse response = bookingService.changeBookingGuestInsuranceStatus(guestId, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<TossCreateOrderResponse> createBookingPayment(@RequestBody BookingPaymentRequest request) {
        // booking details
        BookingResponse booking = bookingService.getBookingById(request.getBookingId());
        BigDecimal totalAmount = bookingService.calculateBookingTotal(request.getBookingId());

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            var preview = voucherService.previewApplyVoucher(
                    com.example.KDBS.dto.request.ApplyVoucherRequest.builder()
                            .bookingId(request.getBookingId())
                            .voucherCode(request.getVoucherCode())
                            .build());
            voucherService.attachVoucherToBookingPending(request.getBookingId(), preview);
            totalAmount = preview.getFinalTotal();
        }

        //  order info
        String orderInfo = String.format(
                "Booking payment for booking ID:%d | Tour:%s - %d guests on %s",
                request.getBookingId(),
                booking.getTourName(),
                booking.getTotalGuests(),
                booking.getDepartureDate());

        TossCreateOrderRequest orderRequest = TossCreateOrderRequest.builder()
                .userEmail(booking.getContactEmail())
                .amount(totalAmount)
                .orderInfo(orderInfo)
                .build();
        var result = tossPaymentService.createOrder(orderRequest);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{bookingId}/company-confirm-completion")
    public ResponseEntity<Void> companyConfirmCompletion(@PathVariable long bookingId) {
        bookingService.confirmedCompletion(bookingId, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{bookingId}/user-confirm-completion")
    public ResponseEntity<Void> userConfirmCompletion(@PathVariable long bookingId) {
        bookingService.confirmedCompletion(bookingId, false);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}/tour-completion-status")
    public ResponseEntity<Boolean> getTourCompletionStatus(@PathVariable long bookingId) {
        Boolean isCompleted = bookingService.getTourCompletionStatus(bookingId);
        return ResponseEntity.ok(isCompleted);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<BookingResponse>> getBookingsByEmail(@PathVariable String email) {
        List<BookingResponse> responses = bookingService.getBookingsByEmail(email);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<BookingWithCountResponse> getBookingsByTourId(@PathVariable Long tourId) {
        List<BookingResponse> responses = bookingService.getBookingsByTourId(tourId);
        return ResponseEntity.ok(new BookingWithCountResponse(responses));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<BookingWithCountResponse> getBookingsByTourCompanyId(@PathVariable Long companyId) {
        BookingWithCountResponse responses = bookingService.getAllBookingsByCompanyId(companyId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/id/{bookingId}/guests")
    public ResponseEntity<List<BookingGuestResponse>> getGuestsByBookingId(@PathVariable Long bookingId) {
        List<BookingGuestResponse> guests = bookingService.getAllGuestsByBookingId(bookingId);
        return ResponseEntity.ok(guests);
    }

    @GetMapping("/summary/email/{email}")
    public ResponseEntity<List<BookingSummaryResponse>> getBookingSummaryByEmail(@PathVariable String email) {
        List<BookingSummaryResponse> responses = bookingService.getBookingSummaryByEmail(email);
        return ResponseEntity.ok(responses);
    }

    // Endpoints with path variables last - use more specific pattern
    @GetMapping("/id/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id/{bookingId}/total")
    public ResponseEntity<Map<String, BigDecimal>> calculateBookingTotal(@PathVariable Long bookingId) {
        BigDecimal total = bookingService.calculateBookingTotal(bookingId);
        return ResponseEntity.ok(Map.of("totalAmount", total));
    }

    @PostMapping("/id/{bookingId}/send-email")
    public ResponseEntity<Map<String, String>> sendBookingEmail(@PathVariable Long bookingId) {
        try {
            bookingService.sendBookingConfirmationEmail(bookingId);
            return ResponseEntity.ok(Map.of("success", "true", "message", "Email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", "false", "message", "Failed to send email: " + e.getMessage()));
        }
    }
}

package com.example.KDBS.controller;

import com.example.KDBS.dto.request.*;
import com.example.KDBS.dto.response.*;
import com.example.KDBS.enums.InsuranceStatus;
import com.example.KDBS.model.Booking;
import com.example.KDBS.service.BookingService;
import com.example.KDBS.service.TossPaymentService;
import com.example.KDBS.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable long bookingId,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.updateBooking(bookingId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable long bookingId) {
        BookingResponse response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cancel/preview/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> previewCancelBooking(@PathVariable long bookingId) {
        BookingResponse response = bookingService.previewCancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-status/{bookingId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<BookingResponse> changeBookingStatus(@PathVariable long bookingId,
                                                               @RequestBody ChangeBookingStatusRequest request) {
        BookingResponse response = bookingService.changeBookingStatus(bookingId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/booking-guest/insurance/change-status/{guestId}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<BookingGuestResponse> changeBookingGuestInsuranceStatus(@PathVariable long guestId,
            @RequestParam InsuranceStatus status) {
        BookingGuestResponse response = bookingService.changeBookingGuestInsuranceStatus(guestId, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TossCreateOrderResponse> createBookingPayment(@RequestBody BookingPaymentRequest request) {
        // booking details
        Booking booking = bookingService.getRealBookingById(request.getBookingId());
        BigDecimal totalAmount = getTotalAmount(request, booking);

        //  order info
        String orderInfo = String.format(
                "Booking payment for booking ID:%d | Tour:%s - %d guests on %s",
                request.getBookingId(),
                booking.getTour().getTourName(),
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

    private static BigDecimal getTotalAmount(BookingPaymentRequest request, Booking booking) {
        BigDecimal totalAmount;
        boolean hasVoucher = booking.getVoucherCode() != null && !booking.getVoucherCode().isBlank();
        boolean isFullPayment = booking.getDepositAmount().equals(booking.getTotalAmount());

        if (isFullPayment) {
            totalAmount = hasVoucher ? booking.getTotalDiscountAmount() : booking.getTotalAmount();
        } else if (request.isDeposit()) {
            totalAmount = hasVoucher ? booking.getDepositDiscountAmount() : booking.getDepositAmount();
        } else {
            // Remaining amount after deposit
            totalAmount = hasVoucher
                    ? booking.getTotalDiscountAmount().subtract(booking.getDepositDiscountAmount())
                    : booking.getTotalAmount().subtract(booking.getDepositAmount());
        }
        return totalAmount;
    }

    @PutMapping("/{bookingId}/company-confirm-completion")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<Void> companyConfirmCompletion(@PathVariable long bookingId) {
        bookingService.confirmedCompletion(bookingId, true);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{bookingId}/user-confirm-completion")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> userConfirmCompletion(@PathVariable long bookingId) {
        bookingService.confirmedCompletion(bookingId, false);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookingId}/complaint")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> createComplaint(@PathVariable long bookingId,
                                                @Valid @RequestBody CreateComplaintRequest request) {
        bookingService.createBookingComplaint(bookingId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/complaint/{complaintId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Void> resolveComplaint(@PathVariable long complaintId,
                                                 @Valid @RequestBody ResolveComplaintRequest request) {
        bookingService.resolveBookingComplaint(complaintId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/complaints/all")
    public ResponseEntity<List<BookingComplaintResponse>> getAllComplaints() {
        List<BookingComplaintResponse> complaints = bookingService.getAllBookingComplaints();
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/id/{complaintId}/complaints")
    public ResponseEntity<BookingComplaintResponse> getComplaintsByComplaintId(@PathVariable Long complaintId) {
        BookingComplaintResponse complaints = bookingService.getComplaintsByComplaintId(complaintId);
        return ResponseEntity.ok(complaints);
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
    public ResponseEntity<BookingWithCountResponse> getBookingsByTourCompanyId(@PathVariable int companyId) {
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

    //COMPANY STATISTIC
    @GetMapping("/company/{companyId}/statistics")
    public ResponseEntity<CompanyBookingStatisticResponse>
    getCompanyBookingStatistics(@PathVariable int companyId) {

        return ResponseEntity.ok(
                bookingService.getCompanyBookingStatistics(companyId)
        );
    }
}

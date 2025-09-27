package com.example.KDBS.controller;

import com.example.KDBS.dto.request.BookingPaymentRequest;
import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.response.BookingResponse;
import com.example.KDBS.dto.response.BookingSummaryResponse;
import com.example.KDBS.dto.response.BookingGuestResponse;
import com.example.KDBS.service.BookingService;
import com.example.KDBS.service.EmailService;
import com.example.KDBS.service.VNPayService;
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
    private final VNPayService vnpayService;
    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> createBookingPayment(@Valid @RequestBody BookingPaymentRequest request) {
        try {
            // booking details
            BookingResponse booking = bookingService.getBookingById(request.getBookingId());
            BigDecimal totalAmount = bookingService.calculateBookingTotal(request.getBookingId());
            
            //  order info
            String orderInfo = String.format("Booking Tour: %s - %s guests on %s", 
                    booking.getTourName(), 
                    booking.getTotalGuests(), 
                    booking.getDepartureDate());

            //  VNPay payment
            var result = vnpayService.createPayment(
                    booking.getContactEmail(), 
                    totalAmount, 
                    orderInfo
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to create payment: " + e.getMessage()
                    ));
        }
    }

    // Specific endpoints first (no path variables)
    @GetMapping("/email/{email}")
    public ResponseEntity<List<BookingResponse>> getBookingsByEmail(@PathVariable String email) {
        List<BookingResponse> responses = bookingService.getBookingsByEmail(email);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByTourId(@PathVariable Long tourId) {
        List<BookingResponse> responses = bookingService.getBookingsByTourId(tourId);
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
            return ResponseEntity.badRequest().body(Map.of("success", "false", "message", "Failed to send email: " + e.getMessage()));
        }
    }
}

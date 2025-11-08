package com.example.KDBS.controller;

import com.example.KDBS.dto.request.BookingPaymentRequest;
import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.request.InsuranceRequest;
import com.example.KDBS.dto.request.TossCreateOrderRequest;
import com.example.KDBS.dto.response.*;
import com.example.KDBS.service.BookingService;
import com.example.KDBS.service.TossPaymentService;
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

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<TossCreateOrderResponse> createBookingPayment(
            @Valid @RequestBody BookingPaymentRequest request) {
        TossCreateOrderResponse resp = tossPaymentService.createOrder(
                TossCreateOrderRequest.builder().bookingId(request.getBookingId()).build()
        );
        return ResponseEntity.ok(resp);
    }

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

    @PostMapping("/insurance/register")
    public ResponseEntity<InsuranceResponse> registerInsurance(@RequestBody InsuranceRequest request) {
        return ResponseEntity.ok(bookingService.registerInsurance(request));
    }
}

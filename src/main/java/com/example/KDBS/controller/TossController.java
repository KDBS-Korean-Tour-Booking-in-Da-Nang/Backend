package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TossConfirmRequest;
import com.example.KDBS.dto.request.TossCreateOrderRequest;
import com.example.KDBS.dto.response.TossConfirmResponse;
import com.example.KDBS.dto.response.TossCreateOrderResponse;
import com.example.KDBS.service.TossPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/toss")
public class TossController {
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final TossPaymentService tossPaymentService;

    @PostMapping("/orders")
    public ResponseEntity<TossCreateOrderResponse> createOrder(
            @Valid @RequestBody TossCreateOrderRequest req) {
        return ResponseEntity.ok(tossPaymentService.createOrder(req));
    }

    @GetMapping("/success")
    public ResponseEntity<?> orderSuccess(@RequestParam TossConfirmRequest request) {
        TossConfirmResponse res = tossPaymentService.confirmPayment(request, true);
        return getFrontendRedirect(res);
    }

    @GetMapping("/fail")
    public ResponseEntity<?> orderFail(@RequestParam TossConfirmRequest request) {
        TossConfirmResponse res = tossPaymentService.confirmPayment(request, false);
        return getFrontendRedirect(res);
    }

    private ResponseEntity<?> getFrontendRedirect(TossConfirmResponse res) {
        if (res != null) {
            // Redirect to frontend result page with info
            String redirectUrl = frontendUrl + "/transaction-result"
                    + "?orderId=" + res.getOrderId()
                    + "&paymentMethod=" + "TOSS"
                    + "&status=" + "SUCCESS";

            return ResponseEntity.status(302)
                    .header("Location", redirectUrl)
                    .body("Redirecting...");
        } else {
            // Transaction not found
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Transaction not found");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

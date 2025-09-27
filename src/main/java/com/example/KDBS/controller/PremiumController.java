package com.example.KDBS.controller;

import com.example.KDBS.dto.request.PremiumPaymentRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.service.EmailService;
import com.example.KDBS.service.PremiumService;
import com.example.KDBS.service.VNPayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/premium")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PremiumController {

    private final PremiumService premiumService;
    private final VNPayService vnPayService;


    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> createPremiumPayment(
            @Valid @RequestBody PremiumPaymentRequest request) {
        
        try {
            // Validate user and get premium details
            premiumService.getUserByEmail(request.getUserEmail()); // Validate user exists
            BigDecimal amount = premiumService.getPremiumPrice(request.getDurationInMonths());
            
            // Create order info
            String orderInfo = String.format(
                    "Premium Upgrade - %d tháng - %s",
                    request.getDurationInMonths(),
                    request.getUserEmail());

            // Create VNPay payment
            var result = vnPayService.createPayment(request.getUserEmail(), amount, orderInfo);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to create payment: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPremiumStatus(Authentication authentication) {
        String userEmail = authentication.getName();
        // Get user and check premium status
        var user = premiumService.getUserByEmail(userEmail);
        boolean isPremium = premiumService.isPremiumActive(user);
        
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .message("Premium status retrieved successfully")
                .result(Map.of("isPremium", isPremium))
                .build());
    }

    @GetMapping("/payment/status/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentStatus(
            @PathVariable String orderId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        var result = premiumService.getPaymentStatus(orderId, userEmail);
        
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .message("Payment status retrieved successfully")
                .result((Map<String, Object>) result)
                .build());
    }

}
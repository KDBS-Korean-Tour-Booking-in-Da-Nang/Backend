package com.example.KDBS.controller;

import com.example.KDBS.model.Transaction;
import com.example.KDBS.repository.TransactionRepository;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.service.VNPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/vnpay")
public class VNPayController {
    private final VNPayService vnpayService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Value("${vnpay.frontend-url}")
    private String frontendUrl;

    public VNPayController(VNPayService vnpayService, UserRepository userRepository,
            TransactionRepository transactionRepository) {
        this.vnpayService = vnpayService;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String userEmail = request.get("userEmail").toString();
            String orderInfo = request.get("orderInfo").toString();

            // Validate user exists
            if (!userRepository.findByEmail(userEmail).isPresent()) {
                throw new RuntimeException("User not found");
            }

            Map<String, Object> result = vnpayService.createPayment(userEmail, amount, orderInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Invalid request data: " + e.getMessage()));
        }
    }

    @GetMapping("/return")
    public ResponseEntity<?> paymentReturn(@RequestParam Map<String, String> params) {
        try {
            // Extract params
            String orderId = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String paymentMethod = "vnpay";

            // Process transaction
            Transaction transaction = vnpayService.processPaymentReturn(params);

            if (transaction != null) {
                // Redirect to frontend result page with info
                String redirectUrl = frontendUrl + "/transaction-result"
                        + "?orderId=" + orderId
                        + "&paymentMethod=" + paymentMethod
                        + "&responseCode=" + responseCode;

                return ResponseEntity.status(302)
                        .header("Location", redirectUrl)
                        .body("Redirecting...");
            } else {
                // Transaction not found
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Transaction not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process payment return");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/transaction/{orderId}")
    public ResponseEntity<?> getTransaction(@PathVariable String orderId) {
        Optional<Transaction> transaction = transactionRepository.findByOrderId(orderId);

        if (transaction.isPresent()) {
            return ResponseEntity.ok(transaction.get());
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Transaction not found");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userEmail}/transactions")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable String userEmail) {
        List<Transaction> transactions = transactionRepository.findByUser_EmailOrderByCreatedTimeDesc(userEmail);
        return ResponseEntity.ok(transactions);
    }
}

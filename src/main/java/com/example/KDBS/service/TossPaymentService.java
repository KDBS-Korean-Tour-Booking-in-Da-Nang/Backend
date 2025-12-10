package com.example.KDBS.service;

import com.example.KDBS.dto.request.TossConfirmRequest;
import com.example.KDBS.dto.request.TossCreateOrderRequest;
import com.example.KDBS.dto.response.TossConfirmResponse;
import com.example.KDBS.dto.response.TossCreateOrderResponse;
import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.enums.PaymentMethod;
import com.example.KDBS.enums.TransactionStatus;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.Transaction;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TransactionRepository;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Service xử lý TossPayments (tạo order + confirm).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

    private final VoucherService voucherService;
    @Value("${toss.client-key}")
    private String clientKey;

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.api-url}")
    private String tossApiUrl;

    @Value("${toss.success-url}")
    private String successUrl;

    @Value("${toss.fail-url}")
    private String failUrl;

    private final BookingRepository bookingRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    public TossCreateOrderResponse createOrder(TossCreateOrderRequest req) {
        // 1) Lấy user
        String tokenEmail = SecurityUtils.getCurrentUserEmail();

        User user = userRepository.findByEmail(tokenEmail)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));

        // 2) Lấy amount từ FE
        BigDecimal amount = req.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }

        // 3) customerKey = safe, theo email
        String customerKey = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("cust:" + req.getUserEmail()).getBytes(StandardCharsets.UTF_8));

        // 4) Tạo orderId duy nhất (dùng cho mọi loại payment)
        String orderId = "ORDER_" + System.currentTimeMillis() + "_" + UUID.randomUUID();

        // 5) Lưu transaction PENDING
        Transaction tx = new Transaction();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setOrderId(orderId);
        tx.setAmount(amount);
        tx.setUser(user);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setPaymentMethod(PaymentMethod.TOSS);
        tx.setOrderInfo(req.getOrderInfo());
        tx.setCreatedTime(LocalDateTime.now());
        tx.setUpdatedTime(LocalDateTime.now());
        transactionRepository.save(tx);

        BigDecimal rateVndToKrw = BigDecimal.ONE
                .divide(BigDecimal.valueOf(18), 6, RoundingMode.HALF_UP);

        BigDecimal amountInKrw = amount.multiply(rateVndToKrw)
                .setScale(0, RoundingMode.HALF_UP);

        // 6) Trả về dữ liệu cho FE gọi Toss widget
        return TossCreateOrderResponse.builder()
                .success(true)
                .clientKey(clientKey)
                .customerKey(customerKey)
                .amount(amountInKrw)
                .orderId(orderId)
                .successUrl(successUrl)
                .failUrl(failUrl)
                .build();
    }

    public TossConfirmResponse confirmPayment(TossConfirmRequest req, boolean isSuccess) {
        try {

            Optional<Transaction> txOpt = transactionRepository.findByOrderId(req.getOrderId());
            Transaction tx = txOpt.orElse(null);

            // Mặc định response DTO
            TossConfirmResponse.TossConfirmResponseBuilder builder = baseConfirmBuilder(req);
            if (isSuccess) {
                if (tx != null) {
                    updateTxAndBooking(tx,
                            TransactionStatus.SUCCESS,
                            BookingStatus.WAITING_FOR_APPROVED,
                            "Toss success");

                    return builder
                            .success(true)
                            .transactionStatus(TransactionStatus.SUCCESS)
                            .orderInfo(tx.getOrderInfo())
                            .responseTime(LocalDateTime.now().toString())
                            .build();
                }
                return null;
            }
            // 5) Thất bại từ Toss → cập nhật FAILED + PENDING booking
            if (tx != null) {
                updateTxAndBooking(tx,
                        TransactionStatus.FAILED,
                        BookingStatus.PENDING_PAYMENT,
                        "Toss failed");
                return builder
                        .success(false)
                        .transactionStatus(TransactionStatus.FAILED)
                        .orderInfo(tx.getOrderInfo())
                        .responseTime(LocalDateTime.now().toString())
                        .build();
            }
            return null;
        } catch (Exception e) {
            log.error("Toss confirm error", e);

            // 6) Runtime error → rollback FAILED/PENDING nếu có transaction
            transactionRepository.findByOrderId(req.getOrderId()).ifPresent(tx ->
                    updateTxAndBooking(tx,
                            TransactionStatus.FAILED,
                            BookingStatus.PENDING_PAYMENT,
                            "Toss runtime error")
            );

            return baseConfirmBuilder(req)
                    .success(false)
                    .transactionStatus(TransactionStatus.FAILED)
                    .responseTime(LocalDateTime.now().toString())
                    .build();
        }
    }

    private TossConfirmResponse.TossConfirmResponseBuilder baseConfirmBuilder(TossConfirmRequest req) {
        return TossConfirmResponse.builder()
                .orderId(req.getOrderId())
                .amount(req.getAmount())
                .payType("TOSS");
    }

    private void updateTxAndBooking(Transaction tx,TransactionStatus txStatus,BookingStatus bookingStatus,String message) {
        tx.setStatus(txStatus);
        tx.setMessage(message);
        tx.setPayType("TOSS");
        tx.setResponseTime(LocalDateTime.now().toString());
        transactionRepository.save(tx);

        Long bookingId = extractBookingIdFromOrderInfo(tx.getOrderInfo());
        if (bookingId != null) {
            bookingRepository.findById(bookingId).ifPresent(b -> {
                b.setBookingStatus(bookingStatus);
                if (txStatus == TransactionStatus.SUCCESS) {
                    voucherService.lockVoucherOnPaymentSuccess(bookingId);
                }
                else {
                    voucherService.unlockVoucherOnBookingCancelled(bookingId);
                }
                bookingRepository.save(b);
            });
        }
    }

    private Long extractBookingIdFromOrderInfo(String orderInfo) {
        try {
            if (orderInfo != null && orderInfo.contains("Booking payment for booking ID:")) {
                String bookingIdStr = orderInfo.replace("Booking payment for booking ID:", "")
                        .split("\\|")[0].trim();
                return Long.parseLong(bookingIdStr);
            }
        } catch (Exception ignore) {}
        return null;
    }
}

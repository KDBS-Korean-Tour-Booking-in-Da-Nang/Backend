package com.example.KDBS.service;

import com.example.KDBS.dto.request.TossConfirmRequest;
import com.example.KDBS.dto.request.TossCreateOrderRequest;
import com.example.KDBS.dto.response.TossCreateOrderResponse;
import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.enums.PaymentMethod;
import com.example.KDBS.enums.TransactionStatus;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.Booking;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.Transaction;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.TransactionRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service xử lý TossPayments (tạo order + confirm).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TossPaymentService {

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
    private final TourRepository tourRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public TossCreateOrderResponse createOrder(TossCreateOrderRequest req) {
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        Tour tour = tourRepository.findById(booking.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Tính tổng tiền booking (giống logic của bạn)
        BigDecimal amount = calcTotalAmount(booking, tour);

        // customerKey: KHÔNG dùng email thô → mã hoá/đặt key ổn định theo user/email
        String safeEmail = booking.getContactEmail() != null ? booking.getContactEmail() : booking.getUserEmail();
        if (safeEmail == null) throw new AppException(ErrorCode.EMAIL_NOT_EXISTED);
        String customerKey = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("cust:" + safeEmail).getBytes(StandardCharsets.UTF_8));

        // orderId unique
        String orderId = "BOOKING_" + booking.getBookingId() + "_" + UUID.randomUUID();

        // Lưu transaction ở trạng thái PENDING
        User user = userRepository.findByEmail(safeEmail)
                .orElse(null); // nếu không có user trong hệ thống thì để null
        Transaction tx = new Transaction();
        tx.setTransactionId(UUID.randomUUID().toString());
        tx.setOrderId(orderId);
        tx.setAmount(amount);
        tx.setUser(user);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setPaymentMethod(PaymentMethod.TOSS);
        tx.setOrderInfo("Booking payment for booking ID:" + booking.getBookingId());
        tx.setCreatedTime(LocalDateTime.now());
        tx.setUpdatedTime(LocalDateTime.now());
        transactionRepository.save(tx);

        return TossCreateOrderResponse.builder()
                .success(true)
                .clientKey(clientKey)
                .customerKey(customerKey)
                .amount(amount)
                .orderId(orderId)
                .successUrl(successUrl) // ex: http://localhost:8080/widget/success.html
                .failUrl(failUrl)       // ex: http://localhost:8080/fail.html
                .build();
    }

    /**
     * Confirm với Toss (paymentKey + orderId + amount)
     * Trả về JSONObject giống response Toss
     */
    public JSONObject confirmPayment(TossConfirmRequest req) {
        try {
            String url = tossApiUrl + "/v1/payments/confirm";

            // Basic Auth: base64(secretKey + ":")
            String basic = Base64.getEncoder()
                    .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + basic);

            JSONObject body = new JSONObject();
            body.put("paymentKey", req.getPaymentKey());
            body.put("orderId", req.getOrderId());
            body.put("amount", req.getAmount()); // Toss yêu cầu number

            HttpEntity<String> entity = new HttpEntity<>(body.toJSONString(), headers);
            ResponseEntity<String> res = restTemplate.postForEntity(url, entity, String.class);

            JSONObject json = (JSONObject) new JSONParser().parse(res.getBody());

            // Cập nhật transaction + booking
            Optional<Transaction> txOpt = transactionRepository.findByOrderId(req.getOrderId());
            if (txOpt.isPresent()) {
                Transaction tx = txOpt.get();
                if (res.getStatusCode().is2xxSuccessful() && json.get("status") != null) {
                    // Thành công
                    tx.setStatus(TransactionStatus.SUCCESS);
                    tx.setMessage("Toss success");
                    tx.setPayType("TOSS");
                    tx.setResponseTime(LocalDateTime.now().toString());
                    transactionRepository.save(tx);

                    Long bookingId = extractBookingIdFromOrderInfo(tx.getOrderInfo());
                    if (bookingId != null) {
                        bookingRepository.findById(bookingId).ifPresent(b -> {
                            b.setBookingStatus(BookingStatus.PURCHASED);
                            bookingRepository.save(b);
                        });
                    }
                } else {
                    // Thất bại
                    tx.setStatus(TransactionStatus.FAILED);
                    tx.setMessage("Toss failed");
                    tx.setPayType("TOSS");
                    tx.setResponseTime(LocalDateTime.now().toString());
                    transactionRepository.save(tx);

                    Long bookingId = extractBookingIdFromOrderInfo(tx.getOrderInfo());
                    if (bookingId != null) {
                        bookingRepository.findById(bookingId).ifPresent(b -> {
                            b.setBookingStatus(BookingStatus.PENDING);
                            bookingRepository.save(b);
                        });
                    }
                    // Ghép error để FE hiển thị
                    if (!json.containsKey("error")) {
                        json.put("error", "Toss confirm failed");
                    }
                }
            }

            return json;
        } catch (Exception e) {
            log.error("Toss confirm error", e);
            JSONObject err = new JSONObject();
            err.put("error", "Runtime error: " + e.getMessage());
            return err;
        }
    }

    private BigDecimal calcTotalAmount(Booking booking, Tour tour) {
        BigDecimal adult = tour.getAdultPrice().multiply(BigDecimal.valueOf(booking.getAdultsCount()));
        BigDecimal child = tour.getChildrenPrice().multiply(BigDecimal.valueOf(booking.getChildrenCount()));
        BigDecimal baby  = tour.getBabyPrice().multiply(BigDecimal.valueOf(booking.getBabiesCount()));
        return adult.add(child).add(baby);
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

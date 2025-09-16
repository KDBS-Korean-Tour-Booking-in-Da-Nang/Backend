package com.example.KDBS.service;

import com.example.KDBS.enums.PaymentMethod;
import com.example.KDBS.enums.TransactionStatus;
import com.example.KDBS.model.Transaction;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class VNPayService {
    @Value("${vnpay.tmnCode}")
    private String vnpTmnCode;
    @Value("${vnpay.secretKey}")
    private String vnpHashSecret;
    @Value("${vnpay.url}")
    private String vnpUrl;
    @Value("${vnpay.returnUrl}")
    private String vnpReturnUrl;

    private final TransactionRepository transactionRepository;
//    private final UserService userService;

    public VNPayService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
//        this.userService = userService;
    }

    public Map<String, Object> createPayment(User user, BigDecimal amount, String orderInfo) {
        try{
            String transactionId = UUID.randomUUID().toString();
            String orderId = "TXN" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);

            Transaction transaction = new Transaction(transactionId, orderId, amount, user, orderInfo );
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setPaymentMethod(PaymentMethod.VNPAY);
            transaction = transactionRepository.save(transaction);

            //Build VNpay parameters
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnpTmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(amount.multiply(new BigDecimal(100)).longValue()));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", "order-type");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
            vnpParams.put("vnp_IpAddr", "127.0.0.1");

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            vnpParams.put("vnp_CreateDate", now.format(formatter));
            vnpParams.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            // Build hash data and query
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
            String paymentUrl = vnpUrl + "?" + query + "&vnp_SecureHash=" + vnpSecureHash;

            transactionRepository.save(transaction);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("payUrl", paymentUrl);
            result.put("transactionId", transactionId);
            result.put("orderId", orderId);
            result.put("orderInfo", orderInfo);

            return result;
        } catch (Exception e){
            throw new RuntimeException("Error creating VNPay payment URL", e);
        }
    }

    public Transaction processPaymentReturn(Map<String, String> vnpParams) throws NoSuchAlgorithmException, InvalidKeyException {
        String vnpSecureHash = vnpParams.get("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHashType");

        // Build hash data
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String signValue = hmacSHA512(vnpHashSecret, hashData.toString());
        String orderId = vnpParams.get("vnp_TxnRef");
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String bankCode = vnpParams.get("vnp_BankCode");
        String payDate = vnpParams.get("vnp_PayDate");
        String message = vnpParams.get("vnp_Message");

        // Find and update transaction
        Optional<Transaction> transactionOpt = transactionRepository.findByOrderId(orderId);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            if (signValue.equals(vnpSecureHash)) {
                if ("00".equals(responseCode)) {
                    transaction.setStatus(TransactionStatus.SUCCESS);
                } else {
                    transaction.setStatus(TransactionStatus.FAILED);
                }
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
            }

            transaction.setResultCode(Integer.parseInt(responseCode));
            transaction.setBankCode(bankCode);
            transaction.setMessage("Topup with VNPay");
            transaction.setResponseTime(payDate);
            transaction.setPayType("VNPAY");

            return transactionRepository.save(transaction);
        }

        return null;
    }

    private String hmacSHA512(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKeySpec);
        byte[] hashBytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

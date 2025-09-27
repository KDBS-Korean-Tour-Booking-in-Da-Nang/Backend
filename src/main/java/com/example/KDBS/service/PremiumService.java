package com.example.KDBS.service;

import com.example.KDBS.enums.PremiumType;
import com.example.KDBS.enums.TransactionStatus;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.Transaction;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.TransactionRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PremiumService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TransactionRepository transactionRepository;

    @Value("${premium.pricing.1month:100000}")
    private BigDecimal oneMonthPrice;

    @Value("${premium.pricing.3months:250000}")
    private BigDecimal threeMonthsPrice;


    public boolean isPremiumActive(User user) {
        if (user.getPremiumType() != PremiumType.PREMIUM) {
            return false;
        }
        
        if (user.getPremiumValidUntil() == null) {
            return false;
        }
        
        return LocalDateTime.now().isBefore(user.getPremiumValidUntil());
    }


    public User getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));
    }

    public BigDecimal getPremiumPrice(int durationInMonths) {
        if (durationInMonths == 1) {
            return oneMonthPrice;
        } else if (durationInMonths == 3) {
            return threeMonthsPrice;
        } else {
            throw new AppException(ErrorCode.INVALID_PREMIUM_DURATION);
        }
    }



    public Map<String, Object> getPaymentStatus(String orderId, String userEmail) {
        try {
            // Find transaction
            Transaction transaction = transactionRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

            // Verify transaction belongs to user
            if (!transaction.getUser().getEmail().equals(userEmail)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            // Check if this is a premium payment
            boolean isPremiumPayment = transaction.getOrderInfo() != null && 
                    transaction.getOrderInfo().startsWith("Premium Upgrade");

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("status", transaction.getStatus().toString());
            result.put("amount", transaction.getAmount());
            result.put("isPremiumPayment", isPremiumPayment);
            result.put("createdTime", transaction.getCreatedTime());
            result.put("updatedTime", transaction.getUpdatedTime());
            
            if (transaction.getStatus() == TransactionStatus.SUCCESS) {
                result.put("message", "Payment successful");
                result.put("isUpgraded", isPremiumActive(transaction.getUser()));
            } else if (transaction.getStatus() == TransactionStatus.FAILED) {
                result.put("message", "Payment failed");
                result.put("isUpgraded", false);
            } else {
                result.put("message", "Payment pending");
                result.put("isUpgraded", false);
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to get payment status for order {}: {}", orderId, e.getMessage(), e);
            throw new AppException(ErrorCode.TRANSACTION_NOT_FOUND);
        }
    }


    public void processPremiumPayment(Transaction transaction) {
        try {

            String orderInfo = transaction.getOrderInfo();
            if (orderInfo != null && orderInfo.startsWith("Premium Upgrade -")) {


                String durationStr = orderInfo.replace("Premium Upgrade -", "")
                        .split("tháng")[0]
                        .trim();
                int durationInMonths = Integer.parseInt(durationStr);
                
                User user = transaction.getUser();
                
                // calculate valid until date
                LocalDateTime currentValidUntil = user.getPremiumValidUntil();
                LocalDateTime newValidUntil;

                if (currentValidUntil == null || currentValidUntil.isBefore(LocalDateTime.now())) {
                    // if not premium or expired, start from now
                    newValidUntil = LocalDateTime.now().plusMonths(durationInMonths);
                } else {
                    // if already premium, extend from current validUntil
                    newValidUntil = currentValidUntil.plusMonths(durationInMonths);
                }

                // Update user premium status
                user.setPremiumType(PremiumType.PREMIUM);
                user.setPremiumValidUntil(newValidUntil);
                userRepository.save(user);

                // Send confirmation email async
                emailService.sendPremiumUpgradeConfirmation(user.getEmail(), durationInMonths, newValidUntil);
                log.info("User {} upgraded to Premium for {} months via payment, valid until {}", 
                        user.getEmail(), durationInMonths, newValidUntil);
            }
        } catch (Exception e) {
            log.error("Error processing premium payment success for transaction: {}", transaction.getOrderId(), e);
        }
    }

}
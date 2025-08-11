package com.example.KDBS.repository;

import com.example.KDBS.model.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPRepository extends JpaRepository<OTP, Long> {
    
    @Query("SELECT o FROM OTP o WHERE o.email = :email AND o.purpose = :purpose AND o.isUsed = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<OTP> findLatestValidOTP(@Param("email") String email, @Param("purpose") String purpose, @Param("now") LocalDateTime now);
    
    @Query("SELECT o FROM OTP o WHERE o.email = :email AND o.purpose = :purpose AND o.otpCode = :otpCode AND o.isUsed = false AND o.expiresAt > :now")
    Optional<OTP> findValidOTP(@Param("email") String email, @Param("purpose") String purpose, @Param("otpCode") String otpCode, @Param("now") LocalDateTime now);
    
    @Query("SELECT o FROM OTP o WHERE o.email = :email AND o.purpose = :purpose AND o.createdAt > :since")
    List<OTP> findRecentOTPs(@Param("email") String email, @Param("purpose") String purpose, @Param("since") LocalDateTime since);
} 
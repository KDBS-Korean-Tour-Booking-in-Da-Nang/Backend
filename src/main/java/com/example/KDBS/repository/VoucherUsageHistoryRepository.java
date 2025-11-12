package com.example.KDBS.repository;

import com.example.KDBS.model.VoucherUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherUsageHistoryRepository extends JpaRepository<VoucherUsageHistory, Long> {
    Optional<VoucherUsageHistory> findTopByBooking_BookingIdOrderByUsedAtDesc(Long bookingId);
}



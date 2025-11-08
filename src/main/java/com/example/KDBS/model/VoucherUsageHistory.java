package com.example.KDBS.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "voucher_usage_history")
public class VoucherUsageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "discount_applied_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal discountAppliedAmount;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    @Column(name = "canceled", nullable = false)
    private boolean canceled;

    @PrePersist
    protected void onCreate() {
        this.usedAt = LocalDateTime.now();
    }
}



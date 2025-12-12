package com.example.KDBS.model;

import com.example.KDBS.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    @Column(name = "booking_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "contact_address")
    private String contactAddress;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "pickup_point")
    private String pickupPoint;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "adults_count")
    private Integer adultsCount;

    @Column(name = "children_count")
    private Integer childrenCount;

    @Column(name = "babies_count")
    private Integer babiesCount;

    @Column(name = "total_guests", nullable = false)
    private Integer totalGuests;

    //Optional fields for when booking have custom message
    @Column(name = "booking_message", length = 500)
    private String bookingMessage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BookingGuest> guests;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "voucher_code", length = 100)
    private String voucherCode;

    @Column(name = "voucher_discount_applied", precision = 15, scale = 2)
    private BigDecimal voucherDiscountApplied;

    @Column(name = "voucher_locked")
    private Boolean voucherLocked;

    @Column(name = "company_confirmed_completion")
    private Boolean companyConfirmedCompletion;

    @Column(name = "user_confirmed_completion")
    private Boolean userConfirmedCompletion;

    @Column(name = "tour_end_date")
    private LocalDate tourEndDate;

    @Column(name = "auto_confirmed_date")
    private LocalDate autoConfirmedDate;

    @Column(name = "auto_failed_date")
    private LocalDate autoFailedDate;

    @Column(name = "min_advance_days")
    private LocalDate minAdvanceDays;

    @Column(name = "cancel_date")
    private LocalDateTime cancelDate;

    @Column(name = "payed_amount")
    private BigDecimal payedAmount;

    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "deposit_discount_amount")
    private BigDecimal depositDiscountAmount;

    @Column(name = "total_discount_amount")
    private BigDecimal totalDiscountAmount;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "refund_percentage")
    private int refundPercentage;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.companyConfirmedCompletion = false;
        this.userConfirmedCompletion = false;
        this.autoConfirmedDate = tourEndDate != null? tourEndDate.plusDays(3) : null;
    }
}

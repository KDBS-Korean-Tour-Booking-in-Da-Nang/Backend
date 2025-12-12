package com.example.KDBS.dto.response;

import com.example.KDBS.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;
    private Long tourId;
    private String userEmail;
    private BookingStatus bookingStatus;
    private String tourName;
    private String contactName;
    private String contactAddress;
    private String contactPhone;
    private String contactEmail;
    private String pickupPoint;
    private String note;
    private LocalDate departureDate;
    private Integer adultsCount;
    private Integer childrenCount;
    private Integer babiesCount;
    private Integer totalGuests;
    private LocalDateTime createdAt;
    private LocalDateTime cancelDate;
    private List<BookingGuestResponse> guests;
    private Boolean companyConfirmedCompletion;
    private Boolean userConfirmedCompletion;
    private LocalDate tourEndDate;
    private LocalDate autoConfirmedDate;
    private LocalDate minAdvanceDays;
    private int bookingCount;
    private String bookingMessage;
    private BigDecimal refundAmount;
    private BigDecimal payedAmount;
    private BigDecimal depositAmount;
    private BigDecimal totalAmount;
    private int refundPercentage;
    private Long voucherId;
    private String voucherCode;
    private BigDecimal voucherDiscountApplied;
    private BigDecimal depositDiscountAmount;  // Final deposit amount after voucher discount
    private BigDecimal totalDiscountAmount; 
}

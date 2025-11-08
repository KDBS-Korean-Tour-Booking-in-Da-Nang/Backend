package com.example.KDBS.model;

import com.example.KDBS.enums.PaymentMethod;
import com.example.KDBS.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "order_info")
    private String orderInfo;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "pay_type")
    private String payType;

    @Column(name = "response_time")
    private String responseTime;

    @Column(name = "result_code")
    private Integer resultCode;

    @Column(name = "message")
    private String message;

    @Column(name = "pay_url")
    private String payUrl;

    @Column(name = "created_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        status = TransactionStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    public Transaction(String transactionId, String orderId, BigDecimal amount, User user, String orderInfo) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.amount = amount;
        this.user = user;
        this.orderInfo = orderInfo;
    }
}

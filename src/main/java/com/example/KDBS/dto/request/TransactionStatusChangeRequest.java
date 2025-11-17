package com.example.KDBS.dto.request;

import com.example.KDBS.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionStatusChangeRequest {
    private String orderId;
    private TransactionStatus status;
}

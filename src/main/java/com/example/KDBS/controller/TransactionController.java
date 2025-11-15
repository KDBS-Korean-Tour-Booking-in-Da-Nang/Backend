package com.example.KDBS.controller;

import com.example.KDBS.dto.response.TransactionResponse;
import com.example.KDBS.enums.TransactionStatus;
import com.example.KDBS.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{userId}")
    public List<TransactionResponse> getAllTransactionsByUserId(@PathVariable int userId) {
        return transactionService.getAllTransactionsByUserId(userId);
    }

    @PutMapping("/{transactionId}/status")
    public TransactionResponse changeTransactionStatus(@PathVariable Long transactionId, @RequestParam TransactionStatus status) {
        return transactionService.changeTransactionStatus(transactionId, status);
    }
}

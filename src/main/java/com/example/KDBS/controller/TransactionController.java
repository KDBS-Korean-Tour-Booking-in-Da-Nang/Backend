package com.example.KDBS.controller;

import com.example.KDBS.model.Transaction;
import com.example.KDBS.service.TransactionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{userEmail}")
    public List<Transaction> getAllTransactionsByUserId(@PathVariable String userEmail) {
        return transactionService.getAllTransactionsByUserEmail(userEmail);
    }
}

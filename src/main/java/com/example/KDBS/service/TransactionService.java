package com.example.KDBS.service;

import com.example.KDBS.model.Transaction;
import com.example.KDBS.repository.TransactionRepository;
import com.example.KDBS.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public void createTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getAllTransactionsByUserEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(transactionRepository::findByUser)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
    }
}

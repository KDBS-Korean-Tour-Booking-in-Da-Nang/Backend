package com.example.KDBS.service;

import com.example.KDBS.dto.request.TransactionStatusChangeRequest;
import com.example.KDBS.dto.response.TransactionResponse;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.TransactionMapper;
import com.example.KDBS.model.Transaction;
import com.example.KDBS.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public List<TransactionResponse> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return transactions.stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }

    public List<TransactionResponse> getAllTransactionsByUserId(int userId) {
        return transactionRepository.findByUser_UserId(userId)
                .stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse changeTransactionStatus(TransactionStatusChangeRequest request) {
        Transaction transaction = transactionRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        transaction.setStatus(request.getStatus());
        return transactionMapper.toTransactionResponse(transaction);
    }
}

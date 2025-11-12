package com.example.KDBS.repository;


import com.example.KDBS.model.Transaction;
import com.example.KDBS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByOrderId(String orderId);
    List<Transaction> findByUser_EmailOrderByCreatedTimeDesc(String userEmail);
    List<Transaction> findByUser(User user);
}

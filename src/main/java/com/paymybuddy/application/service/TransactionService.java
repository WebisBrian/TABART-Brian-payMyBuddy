package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TransactionService {

    void transfer(Long senderId, Long receiverId, BigDecimal amount, String description);

    Page<Transaction> getTransactionHistory(Long userId, Pageable pageable);
}

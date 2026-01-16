package com.paymybuddy.application.service;

import java.math.BigDecimal;

public interface TransactionService {

    void transfer(Long senderId, Long receiverId, BigDecimal amount, String description);
}

package com.paymybuddy.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        return BigDecimal.ZERO;
    }

    @Transactional
    public void deposit(Long userId, BigDecimal amount) {

    }

    @Transactional
    public void withdraw(Long userId, BigDecimal amount) {

    }
}

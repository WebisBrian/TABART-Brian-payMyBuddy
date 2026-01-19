package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        return account.getBalance();
    }

    @Transactional
    public void deposit(Long userId, BigDecimal amount) {

    }

    @Transactional
    public void withdraw(Long userId, BigDecimal amount) {

    }
}

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

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        return account.getBalance();
    }

    @Override
    @Transactional
    public void deposit(Long userId, BigDecimal amount) {
        if (userId == null || amount == null) {
            throw new IllegalArgumentException("User ID and amount must not be null.");
        }

        accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."))
                .deposit(amount);
    }

    @Override
    @Transactional
    public void withdraw(Long userId, BigDecimal amount) {
        if (userId == null || amount == null) {
            throw new IllegalArgumentException("User ID and amount must not be null.");
        }

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        // Check for sufficient balance, today an account can't be negative, tomorrow
        // maybe we'll have negative balance, that's why we don't check this in entity.
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        account.withdraw(amount);
    }
}

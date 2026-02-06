package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.InvalidUserIdException;
import com.paymybuddy.application.service.exception.UserAccountNotFoundException;
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
        ensureUserIdNotNull(userId);

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new UserAccountNotFoundException(userId));

        return account.getBalance();
    }

    @Override
    @Transactional
    public void deposit(Long userId, BigDecimal amount) {
        ensureUserIdNotNull(userId);

        accountRepository.findByUserId(userId)
                .orElseThrow(() -> new UserAccountNotFoundException(userId))
                .deposit(amount);
    }

    @Override
    @Transactional
    public void withdraw(Long userId, BigDecimal amount) {
        ensureUserIdNotNull(userId);

        accountRepository.findByUserId(userId)
                .orElseThrow(() -> new UserAccountNotFoundException(userId))
                .withdraw(amount);
    }

    /* ---------- Helpers ---------- */
    private static void ensureUserIdNotNull(Long userId) {
        if (userId == null) {
            throw new InvalidUserIdException();
        }
    }
}

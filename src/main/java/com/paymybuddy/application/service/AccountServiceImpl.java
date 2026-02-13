package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.AccountNotFoundException;
import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        logger.debug("getBalance called: userId={}", userId);
        ensureUserIdNotNull(userId);

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Account not found while getting balance: userId={}", userId);
                    return new AccountNotFoundException(userId);
                });

        logger.debug("Balance retrieved: userId={}, balance={}", userId, account.getBalance());
        return account.getBalance();
    }

    @Override
    @Transactional
    public void deposit(Long userId, BigDecimal amount) {
        logger.debug("deposit called: userId={}, amount={}", userId, amount);
        ensureUserIdNotNull(userId);

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                        logger.warn("Account not found while depositing: userId={}, amount={}", userId, amount);
                        return new AccountNotFoundException(userId);
                });

        account.deposit(amount);
        logger.info("Deposit completed: userId={}, amount={}", userId, amount);
    }

    @Override
    @Transactional
    public void withdraw(Long userId, BigDecimal amount) {
        logger.debug("withdraw called: userId={}, amount={}", userId, amount);
        ensureUserIdNotNull(userId);

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Account not found while withdrawing: userId={}, amount={}", userId, amount);
                    return new AccountNotFoundException(userId);
                });

        account.withdraw(amount);
        logger.info("Withdraw completed: userId={}, amount={}", userId, amount);
    }

    /* ---------- Helpers ---------- */
    private static void ensureUserIdNotNull(Long userId) {
        if (userId == null) {
            logger.warn("Invalid argument: userId is null");
            throw new IllegalArgumentException("User ID must not be null.");
        }
    }
}

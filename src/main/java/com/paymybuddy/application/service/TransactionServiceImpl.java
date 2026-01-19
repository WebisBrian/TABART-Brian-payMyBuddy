package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.Transaction;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import com.paymybuddy.infrastructure.repository.TransactionRepository;
import com.paymybuddy.infrastructure.repository.UserContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {

//    TODO : implements constant for fee
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserContactRepository userContactRepository;
    private final Clock clock;

    public TransactionServiceImpl(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository,
                                  UserContactRepository userContactRepository,
                                  Clock clock) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userContactRepository = userContactRepository;
        this.clock = clock;
    }

    /* ---------- transfer() ---------- */
    @Transactional
    public void transfer(Long senderId, Long receiverId, BigDecimal amount, String description) {

        validateInput(senderId, receiverId, amount);

        Account senderAccount = loadAccountByUserId(senderId, "Sender account not found.");
        Account receiverAccount = loadAccountByUserId(receiverId, "Receiver account not found.");

        ensureUsersAreContacts(senderId, receiverId);

        BigDecimal fee = feeOf(amount);
        BigDecimal totalDebit = amount.add(fee);

        ensureSufficientBalance(senderAccount, totalDebit);

        senderAccount.withdraw(totalDebit);
        receiverAccount.deposit(amount);

        LocalDateTime now = LocalDateTime.now(clock);
        Transaction transaction = Transaction.create(
                senderAccount,
                receiverAccount,
                amount,
                fee,
                now,
                description
        );

        transactionRepository.save(transaction);
    }

    private void validateInput(Long senderId, Long receiverId, BigDecimal amount) {
        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("Sender and receiver IDs must not be null.");
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver must be different users.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
    }

    private Account loadAccountByUserId(Long userId, String errorMessage) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }

    private void ensureUsersAreContacts(Long senderId, Long receiverId) {
        boolean isContact = userContactRepository.existsByUser_IdAndContact_Id(senderId, receiverId);
        if (!isContact) {
            throw new IllegalArgumentException("Sender and receiver must be contacts.");
        }
    }

    private static void ensureSufficientBalance(Account senderAccount, BigDecimal totalDebit) {
        if (senderAccount.getBalance().compareTo(totalDebit) < 0) {
            throw new IllegalArgumentException("Insufficient balance in sender account.");
        }
    }

    /**
     * Fee = 0.5% (0.005) of the amount, rounded to 2 decimal places (HALF_UP).
     * Example: 100.00 -> 0.50
     */
    private static BigDecimal feeOf(BigDecimal amount) {
        return amount
                .multiply(new BigDecimal("0.005"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /* ---------- getTransaction() ---------- */
    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionHistory(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }

        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null.");
        }

        Account account = loadAccountByUserId(userId, "Account not found.");

        return transactionRepository.findTransactionHistory(account.getId(), pageable);
    }
}

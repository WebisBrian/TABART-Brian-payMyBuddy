package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.AccountNotFoundException;
import com.paymybuddy.application.service.exception.NotInContactsException;
import com.paymybuddy.application.service.exception.SelfTransferException;
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

    private static final BigDecimal FEE_RATE = new BigDecimal("0.005");

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

    @Override
    @Transactional
    public void transfer(Long senderId, Long receiverId, BigDecimal amount, String description) {
        validateInput(senderId, receiverId, amount);

        Account senderAccount = loadAccountByUserId(senderId);
        Account receiverAccount = loadAccountByUserId(receiverId);

        ensureUsersAreContacts(senderId, receiverId);

        // Fee calculation
        BigDecimal fee = calculateFee(amount);
        BigDecimal totalDebit = amount.add(fee);

        // Amount debited from sender
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

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactionHistory(Long userId, Pageable pageable) {
        requireNonNull(userId, "User ID");
        requireNonNull(pageable, "Pageable");

        Account account = loadAccountByUserId(userId);

        return transactionRepository.findTransactionHistory(account.getId(), pageable);
    }

    /* ---------- Helpers ---------- */
    private void validateInput(Long senderId, Long receiverId, BigDecimal amount) {
        requireNonNull(senderId, "Sender ID");
        requireNonNull(receiverId, "Receiver ID");
        requireNonNull(amount, "Amount");

        if (senderId.equals(receiverId)) {
            throw new SelfTransferException(senderId, receiverId);
        }
    }

    private Account loadAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AccountNotFoundException(userId));
    }

    private void ensureUsersAreContacts(Long senderId, Long receiverId) {
        if (!userContactRepository.existsByUser_IdAndContact_Id(senderId, receiverId)) {
            throw new NotInContactsException(senderId, receiverId);
        }
    }

    /**
     * Example:
     * If Fee = 0.5% (0.005) of the amount, rounded to 2 decimal places (HALF_UP).
     * 100.00 -> 0.50
     */
    private static BigDecimal calculateFee(BigDecimal amount) {
        return amount
                .multiply(FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static void requireNonNull(Object value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " must not be null.");
        }
    }
}

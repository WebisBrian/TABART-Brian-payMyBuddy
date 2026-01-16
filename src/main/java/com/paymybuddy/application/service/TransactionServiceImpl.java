package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.Transaction;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import com.paymybuddy.infrastructure.repository.TransactionRepository;
import com.paymybuddy.infrastructure.repository.UserContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class TransactionServiceImpl implements TransactionService {

//    TODO : implements constant for fee
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserContactRepository userContactRepository;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, UserContactRepository userContactRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userContactRepository = userContactRepository;
    }

    @Transactional
    public void transfer(Long senderId, Long receiverId, BigDecimal amount, String description) {

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("Sender and receiver IDs must not be null.");
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver must be different users.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        Account senderAccount = accountRepository.findByUserId(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found."));

        Account receiverAccount = accountRepository.findByUserId(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found."));

        boolean isContact = userContactRepository.existsByUser_IdAndContact_Id(senderId, receiverId);
        if (!isContact) {
            throw new IllegalArgumentException("Sender and receiver must be contacts.");
        }

        BigDecimal fee = feeOf(amount);
        BigDecimal totalDebit = amount.add(fee);

        if (senderAccount.getBalance().compareTo(totalDebit) < 0) {
            throw new IllegalArgumentException("Insufficient balance in sender account.");
        }

        senderAccount.withdraw(totalDebit);
        receiverAccount.deposit(amount);

        Transaction transaction = Transaction.create(
                senderAccount,
                receiverAccount,
                amount,
                fee,
                LocalDateTime.now(),
                description
        );

        transactionRepository.save(transaction);
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
}

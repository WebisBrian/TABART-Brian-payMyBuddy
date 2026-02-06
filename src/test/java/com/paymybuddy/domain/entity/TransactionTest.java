package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    /* ---------- create() ---------- */
    @Test
    void create_shouldCreateTransaction_whenValid() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        BigDecimal amount = new BigDecimal("10.00");
        BigDecimal fee = new BigDecimal("0.50");
        LocalDateTime date = LocalDateTime.now();
        String description = "Dinner refund";

        Transaction tx = Transaction.create(sender, receiver, amount, fee, date, description);

        assertNotNull(tx);
        assertEquals(sender, tx.getSenderAccount());
        assertEquals(receiver, tx.getReceiverAccount());
        assertEquals(amount, tx.getAmount());
        assertEquals(fee, tx.getFee());
        assertEquals(date, tx.getDate());
        assertEquals(description, tx.getDescription());
    }

    @Test
    void create_shouldThrow_whenSenderAccountIsNull() {
        Account receiver = validAccount("receiver@mail.com");

        MissingSenderAccountException ex = assertThrows(MissingSenderAccountException.class, () ->
                Transaction.create(null, receiver, new BigDecimal("10.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Sender account is required"));
    }

    @Test
    void create_shouldThrow_whenReceiverAccountIsNull() {
        Account sender = validAccount("sender@mail.com");

        MissingReceiverAccountException ex = assertThrows(MissingReceiverAccountException.class, () ->
                Transaction.create(sender, null, new BigDecimal("10.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Receiver account is required"));
    }

    @Test
    void create_shouldThrow_whenAmountIsNull() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        InvalidTransactionAmountException ex = assertThrows(InvalidTransactionAmountException.class, () ->
                Transaction.create(sender, receiver, null, BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Amount must be strictly positive."));
    }

    @Test
    void create_shouldThrow_whenAmountIsZeroOrNegative() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        // Test zero amount
        InvalidTransactionAmountException exZero = assertThrows(InvalidTransactionAmountException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("0.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );
        assertTrue(exZero.getMessage().contains("Amount must be strictly positive."));

        // Test negative amount
        InvalidTransactionAmountException exNegative = assertThrows(InvalidTransactionAmountException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("-1.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );
        assertTrue(exNegative.getMessage().contains("Amount must be strictly positive."));
    }

    @Test
    void create_shouldThrow_whenFeeIsNull() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        InvalidTransactionFeeException ex = assertThrows(InvalidTransactionFeeException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("10.00"), null, LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Fee must be zero or positive."));
    }

    @Test
    void create_shouldThrow_whenFeeIsNegative() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        InvalidTransactionFeeException ex = assertThrows(InvalidTransactionFeeException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("10.00"), new BigDecimal("-0.01"), LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Fee must be zero or positive."));
    }

    @Test
    void create_shouldThrow_whenDateIsNull() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        MissingTransactionDateException ex = assertThrows(MissingTransactionDateException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("10.00"), BigDecimal.ZERO, null, null)
        );

        assertTrue(ex.getMessage().contains("Transaction date is required."));
    }

    // Helpers
    private static Account validAccount(String email) {
        User user = User.create("User", email, "hash");
        return Account.create(user);
    }
}
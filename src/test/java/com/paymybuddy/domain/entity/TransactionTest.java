package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    /* ---------- create() - Happy paths ---------- */
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

    /* ---------- create() - Validation errors ---------- */
    @Test
    void create_shouldThrow_whenSenderAccountIsNull() {
        Account receiver = validAccount("receiver@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(null, receiver, new BigDecimal("10.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Sender account is required"));
    }

    @Test
    void create_shouldThrow_whenReceiverAccountIsNull() {
        Account sender = validAccount("sender@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, null, new BigDecimal("10.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Receiver account is required"));
    }

    @Test
    void create_shouldThrow_whenAmountIsNull() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () ->
                Transaction.create(sender, receiver, null, BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Amount must be strictly positive."));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0})
    void create_shouldThrow_whenAmountIsZeroOrNegative(Double amount) {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal(amount), BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Amount must be strictly positive."));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {-1.0})
    void create_shouldThrow_whenFeeIsNullOrNegative(Double fee) {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("10.00"), fee == null ? null : BigDecimal.valueOf(fee), LocalDateTime.now(), null)
        );

        assertTrue(ex.getMessage().contains("Fee must be zero or positive."));
    }

    @Test
    void create_shouldThrow_whenDateIsNull() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("10.00"), BigDecimal.ZERO, null, null)
        );

        assertTrue(ex.getMessage().contains("Transaction date is required."));
    }

    /* ---------- Helpers ---------- */
    private static Account validAccount(String email) {
        User user = User.create("User", email, "hash");
        return Account.create(user);
    }
}
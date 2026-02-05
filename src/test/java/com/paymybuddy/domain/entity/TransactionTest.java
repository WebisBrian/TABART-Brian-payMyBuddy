package com.paymybuddy.domain.entity;

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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(null, receiver, new BigDecimal("10.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertEquals("Sender account is required.", ex.getMessage());
    }

    @Test
    void create_shouldThrow_whenReceiverAccountIsNull() {
        Account sender = validAccount("sender@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, null, new BigDecimal("10.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertEquals("Receiver account is required.", ex.getMessage());
    }

    @Test
    void create_shouldThrow_whenAmountIsNull() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, receiver, null, BigDecimal.ZERO, LocalDateTime.now(), null)
        );

        assertEquals("Amount must be positive.", ex.getMessage());
    }

    @Test
    void create_shouldThrow_whenAmountIsZeroOrNegative() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        IllegalArgumentException exZero = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("0.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );
        assertEquals("Amount must be positive.", exZero.getMessage());

        IllegalArgumentException exNegative = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("-1.00"), BigDecimal.ZERO, LocalDateTime.now(), null)
        );
        assertEquals("Amount must be positive.", exNegative.getMessage());
    }

    @Test
    void create_shouldThrow_whenFeeIsNull() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("10.00"), null, LocalDateTime.now(), null)
        );

        assertEquals("Fee must be zero or positive.", ex.getMessage());
    }

    @Test
    void create_shouldThrow_whenFeeIsNegative() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("10.00"), new BigDecimal("-0.01"), LocalDateTime.now(), null)
        );

        assertEquals("Fee must be zero or positive.", ex.getMessage());
    }

    @Test
    void create_shouldThrow_whenDateIsNull() {
        Account sender = validAccount("sender@mail.com");
        Account receiver = validAccount("receiver@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Transaction.create(sender, receiver, new BigDecimal("10.00"), BigDecimal.ZERO, null, null)
        );

        assertEquals("Date is required.", ex.getMessage());
    }

    // Helpers
    private static Account validAccount(String email) {
        User user = User.create("User", email, "hash");
        return Account.create(user);
    }
}
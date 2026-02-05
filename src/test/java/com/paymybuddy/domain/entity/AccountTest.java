package com.paymybuddy.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    /* ---------- create() ---------- */
    @Test
    void create_shouldCreateAccount_withZeroBalance_andUserSet() {
        User user = validUser("user@mail.com");

        Account account = Account.create(user);

        assertNotNull(account);
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(user, account.getUser());
    }

    @Test
    void create_shouldThrow_whenUserIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Account.create(null)
        );

        assertEquals("User must not be null.", ex.getMessage());
    }

    /* ---------- deposit() ---------- */
    @Test
    void deposit_shouldIncreaseBalance_whenAmountIsPositive() {
        Account account = newAccount("user@mail.com");

        account.deposit(new BigDecimal("10.00"));

        assertEquals(new BigDecimal("10.00"), account.getBalance());
    }

    @Test
    void deposit_shouldThrow_whenAmountIsNull() {
        Account account = newAccount("user@mail.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                account.deposit(null)
        );

        assertEquals("Amount must be positive", ex.getMessage());
    }

    @Test
    void deposit_shouldThrow_whenAmountIsZeroOrNegative() {
        Account account = newAccount("user@mail.com");

        IllegalArgumentException exZero = assertThrows(IllegalArgumentException.class, () ->
                account.deposit(new BigDecimal("0.00"))
        );
        assertEquals("Amount must be positive", exZero.getMessage());

        IllegalArgumentException exNegative = assertThrows(IllegalArgumentException.class, () ->
                account.deposit(new BigDecimal("-1.00"))
        );
        assertEquals("Amount must be positive", exNegative.getMessage());
    }

    /* ---------- withdraw() ---------- */
    @Test
    void withdraw_shouldDecreaseBalance_whenSufficientBalance() {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("20.00"));

        account.withdraw(new BigDecimal("7.50"));

        assertEquals(new BigDecimal("12.50"), account.getBalance());
    }

    @Test
    void withdraw_shouldThrow_whenAmountIsNull() {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("10.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                account.withdraw(null)
        );

        assertEquals("Amount must be positive", ex.getMessage());
    }

    @Test
    void withdraw_shouldThrow_whenAmountIsZeroOrNegative() {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("10.00"));

        IllegalArgumentException exZero = assertThrows(IllegalArgumentException.class, () ->
                account.withdraw(new BigDecimal("0.00"))
        );
        assertEquals("Amount must be positive", exZero.getMessage());

        IllegalArgumentException exNegative = assertThrows(IllegalArgumentException.class, () ->
                account.withdraw(new BigDecimal("-0.01"))
        );
        assertEquals("Amount must be positive", exNegative.getMessage());
    }

    @Test
    void withdraw_shouldThrow_whenInsufficientBalance() {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("5.00"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                account.withdraw(new BigDecimal("5.01"))
        );

        assertEquals("Insufficient balance.", ex.getMessage());
        // Verify that balance did not change
        assertEquals(new BigDecimal("5.00"), account.getBalance());
    }

    // Helpers
    private static Account newAccount(String email) {
        return Account.create(validUser(email));
    }

    private static User validUser(String email) {
        return User.create("User", email, "hash");
    }
}

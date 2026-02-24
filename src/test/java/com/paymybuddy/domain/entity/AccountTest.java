package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.InsufficientBalanceException;
import com.paymybuddy.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    /* ---------- create() - Happy paths ---------- */
    @Test
    void create_shouldCreateAccount_withZeroBalance_andUserSet() {
        User user = validUser("user@mail.com");

        Account account = Account.create(user);

        assertNotNull(account);
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(user, account.getUser());
    }

    /* ---------- create() - Validation errors ---------- */
    @Test
    void create_shouldThrow_whenUserIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Account.create(null)
        );
        assertTrue(ex.getMessage().contains("Account must be associated with a user."));
    }

    /* ---------- deposit() - Happy paths ---------- */
    @Test
    void deposit_shouldIncreaseBalance_whenAmountIsPositive() {
        Account account = newAccount("user@mail.com");

        account.deposit(new BigDecimal("10.00"));

        assertEquals(new BigDecimal("10.00"), account.getBalance());
    }

    /* ---------- deposit() - Validation errors ---------- */
    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {0.0, -1.0})
    void deposit_shouldThrow_whenAmountIsInvalid(Double amount) {
        Account account = newAccount("user@mail.com");

        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () ->
                account.deposit(amount == null ? null : BigDecimal.valueOf(amount))
        );

        assertTrue(ex.getMessage().contains("Amount must be strictly positive."));
    }

    /* ---------- withdraw() - Happy paths ---------- */
    @Test
    void withdraw_shouldDecreaseBalance_whenSufficientBalance() {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("20.00"));

        account.withdraw(new BigDecimal("7.50"));

        assertEquals(new BigDecimal("12.50"), account.getBalance());
    }

    /* ---------- withdraw() - Validation errors ---------- */
    @ParameterizedTest
    @NullSource
    @ValueSource(doubles = {0.0, -1.0})
    void withdraw_shouldThrow_whenAmountIsInvalid(Double amount) {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("10.00"));

        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () ->
                account.withdraw(amount == null ? null : BigDecimal.valueOf(amount)));

        assertTrue(ex.getMessage().contains("Amount must be strictly positive."));
    }

    @Test
    void withdraw_shouldThrow_whenInsufficientBalance() {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("5.00"));

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class, () ->
                account.withdraw(new BigDecimal("5.01"))
        );

        assertTrue(ex.getMessage().contains("Insufficient balance."));
        // Verify that the balance did not change
        assertEquals((new BigDecimal("5.00")), account.getBalance());
    }

    /* ---------- Helpers ---------- */
    private static Account newAccount(String email) {
        return Account.create(validUser(email));
    }

    private static User validUser(String email) {
        return User.create("User", email, "hash");
    }
}

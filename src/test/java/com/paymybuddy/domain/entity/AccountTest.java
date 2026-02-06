package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.InsufficientBalanceException;
import com.paymybuddy.domain.exception.InvalidAmountException;
import com.paymybuddy.domain.exception.MissingAccountOwnerException;
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
        MissingAccountOwnerException ex = assertThrows(MissingAccountOwnerException.class, () ->
                Account.create(null)
        );

        assertEquals("Account must be associated with a user.", ex.getMessage());
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

        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () ->
                account.deposit(null)
        );

        assertEquals("Amount must be strictly positive. Provided: " + null, ex.getMessage());
    }

    @Test
    void deposit_shouldThrow_whenAmountIsZeroOrNegative() {
        Account account = newAccount("user@mail.com");
        BigDecimal amountZero = BigDecimal.ZERO;
        BigDecimal amountNegative = new BigDecimal("-1.00");

        InvalidAmountException exZero = assertThrows(InvalidAmountException.class, () ->
                account.deposit(amountZero)
        );
        assertEquals("Amount must be strictly positive. Provided: " + amountZero, exZero.getMessage());

        InvalidAmountException exNegative = assertThrows(InvalidAmountException.class, () ->
                account.deposit(amountNegative)
        );
        assertEquals("Amount must be strictly positive. Provided: " + amountNegative, exNegative.getMessage());
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

        InvalidAmountException ex = assertThrows(InvalidAmountException.class, () ->
                account.withdraw(null)
        );

        assertEquals("Amount must be strictly positive. Provided: " + null, ex.getMessage());
    }

    @Test
    void withdraw_shouldThrow_whenAmountIsZeroOrNegative() {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("10.00"));
        BigDecimal amountZero = BigDecimal.ZERO;
        BigDecimal amountNegative = new BigDecimal("-1.00");

        InvalidAmountException exZero = assertThrows(InvalidAmountException.class, () ->
                account.withdraw(amountZero)
        );
        assertEquals("Amount must be strictly positive. Provided: " + amountZero, exZero.getMessage());

        InvalidAmountException exNegative = assertThrows(InvalidAmountException.class, () ->
                account.withdraw(amountNegative)
        );
        assertEquals("Amount must be strictly positive. Provided: " + amountNegative, exNegative.getMessage());
    }

    @Test
    void withdraw_shouldThrow_whenInsufficientBalance() {
        Account account = newAccount("user@mail.com");
        BigDecimal balance = new BigDecimal("5.00");
        account.deposit(balance);

        BigDecimal amount = new BigDecimal("5.01");
        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class, () ->
                account.withdraw(amount)
        );

        assertEquals("Insufficient balance. Balance: " + balance + ", Amount: " + amount, ex.getMessage());
        // Verify that the balance did not change
        assertEquals(balance, account.getBalance());
    }

    /* ---------- Helpers ---------- */
    private static Account newAccount(String email) {
        return Account.create(validUser(email));
    }

    private static User validUser(String email) {
        return User.create("User", email, "hash");
    }
}

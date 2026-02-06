package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.InsufficientBalanceException;
import com.paymybuddy.domain.exception.InvalidMoneyAmountException;
import com.paymybuddy.domain.exception.MissingAccountOwnerException;
import org.junit.jupiter.api.Assertions;
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
        assertTrue(ex.getMessage().contains("Account must be associated with a user."));
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

        InvalidMoneyAmountException ex = assertThrows(InvalidMoneyAmountException.class, () ->
                account.deposit(null)
        );

        assertTrue(ex.getMessage().contains("Amount must be strictly positive."));
    }

    @Test
    void deposit_shouldThrow_whenAmountIsZeroOrNegative() {
        Account account = newAccount("user@mail.com");

        // Test zero amount
        InvalidMoneyAmountException exZero = assertThrows(InvalidMoneyAmountException.class, () ->
                account.deposit(BigDecimal.ZERO)
        );
        InvalidMoneyAmountException exNegative;
        assertTrue(exZero.getMessage().contains("Amount must be strictly positive."));

        // Test negative amount
        exNegative = Assertions.assertThrows(InvalidMoneyAmountException.class, () ->
                account.deposit(new BigDecimal("-1.00"))
        );
        assertTrue(exNegative.getMessage().contains("Amount must be strictly positive."));
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

        InvalidMoneyAmountException ex = assertThrows(InvalidMoneyAmountException.class, () ->
                account.withdraw(null)
        );

        assertTrue(ex.getMessage().contains("Amount must be strictly positive."));
    }

    @Test
    void withdraw_shouldThrow_whenAmountIsZeroOrNegative() {
        Account account = newAccount("user@mail.com");
        account.deposit(new BigDecimal("10.00"));

        // Test zero amount
        InvalidMoneyAmountException exZero = assertThrows(InvalidMoneyAmountException.class, () ->
                account.withdraw(BigDecimal.ZERO)
        );
        assertTrue(exZero.getMessage().contains("Amount must be strictly positive."));

        // Test negative amount
        InvalidMoneyAmountException exNegative = assertThrows(InvalidMoneyAmountException.class, () ->
                account.withdraw(new BigDecimal("-1.00"))
        );
        assertTrue(exNegative.getMessage().contains("Amount must be strictly positive."));
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

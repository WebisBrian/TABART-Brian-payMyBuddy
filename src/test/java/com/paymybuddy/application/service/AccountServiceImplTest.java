package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    /* ---------- getBalance() ---------- */
    @Test
    void getBalance_shouldReturnBalance_whenAccountExists() {
        long userId = 1L;
        User user = User.create("user", "user@email.com", "password");
        Account account = Account.create(user);
        account.deposit(new BigDecimal("150.00"));

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        BigDecimal balance = accountService.getBalance(userId);

        assertThat(balance).isEqualByComparingTo("150.00");

        verify(accountRepository).findByUserId(userId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void getBalance_shouldThrow_whenAccountNotFound() {
        long userId = 1L;

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getBalance(userId))
                .isInstanceOf(RuntimeException.class);

        verify(accountRepository).findByUserId(userId);
        verifyNoMoreInteractions(accountRepository);
    }

    /* ---------- deposit() ---------- */
    @Test
    void deposit_shouldIncreaseBalance_whenValidAmount() {
        long userId = 1L;
        User user = User.create("user", "user@email.com", "password");
        Account account = Account.create(user);

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        accountService.deposit(userId, new BigDecimal("95.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("95.00");
        verify(accountRepository).findByUserId(userId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void deposit_shouldThrow_whenInvalidAmount() {
        long userId = 1L;
        User user = User.create("user", "user@email.com", "password");
        Account account = Account.create(user);

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.deposit(userId, new BigDecimal("-95.00")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
        verify(accountRepository).findByUserId(userId);
        verifyNoMoreInteractions(accountRepository);
    }

    /* ---------- withdraw() ---------- */
    @Test
    void withdraw_shouldDecreaseBalance_whenSufficientBalance() {
        long userId = 1L;
        User user = User.create("user", "user@email.com", "password");
        Account account = Account.create(user);
        account.deposit(new BigDecimal("150.00"));

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        accountService.withdraw(userId, new BigDecimal("50.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("100.00");
        verify(accountRepository).findByUserId(userId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void withdraw_shouldThrow_whenInsufficientBalance() {
        long userId = 1L;
        User user = User.create("user", "user@email.com", "password");
        Account account = Account.create(user);

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(userId, new BigDecimal("50.00")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
        verify(accountRepository).findByUserId(userId);
        verifyNoMoreInteractions(accountRepository);
    }
}
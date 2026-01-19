package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void deposit_shouldIncreaseBalance_whenValidAmount() {

    }

    @Test
    void withdraw_shouldDecreaseBalance_whenSufficientBalance() {

    }

    @Test
    void withdraw_shouldThrow_whenInsufficientBalance() {

    }
}
package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationServiceImpl registerService;

    /* ---------- register() ---------- */
    @Test
    void register_shouldCreateUserAndAccount_whenValid() {
        when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("bcrypt-hash");

        // emulate JPA returning saved entities
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        registerService.register("user", "user@email.com", "password");

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("user");
        assertThat(savedUser.getEmail()).isEqualTo("user@email.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("bcrypt-hash");

        // Account saved and linked to the same user
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());

        Account savedAccount = accountCaptor.getValue();
        assertThat(savedAccount.getUser()).isSameAs(savedUser);

        verify(passwordEncoder).encode("password");
    }

    @Test
    void register_shouldThrow_whenUsernameIsNull() {
        assertThatThrownBy(() -> registerService.register(null, "user@email.com", "password"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, accountRepository, passwordEncoder);
    }

    @Test
    void register_shouldThrow_whenEmailIsNull() {
        assertThatThrownBy(() -> registerService.register("user", null, "password"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, accountRepository, passwordEncoder);
    }

    @Test
    void register_shouldThrow_whenPasswordIsNull() {
        assertThatThrownBy(() -> registerService.register("user", "user@email.com", null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, accountRepository, passwordEncoder);
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
     when(userRepository.existsByEmail("user@email.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registerService.register("user", "user@email.com", "password"))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any(User.class));
        verify(accountRepository, never()).save(any(Account.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}
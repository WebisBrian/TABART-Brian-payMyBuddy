package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.application.service.exception.WeakPasswordException;
import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.exception.InvalidEmailException;
import com.paymybuddy.domain.exception.InvalidUserFieldException;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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
    private RegistrationServiceImpl registrationService;

    /* ---------- constants ---------- */
    private static final String USERNAME = "user";
    private static final String EMAIL = "user@email.com";
    private static final String PASSWORD = "Password123";
    private static final String PASSWORD_HASH = "hashedPassword";

    /* ---------- register() - Happy path ---------- */
    @Test
    void register_shouldCreateUserAndAccount_whenDataIsValid() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);

        User mockUser = User.create(USERNAME, EMAIL, PASSWORD_HASH);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(accountRepository.save(any(Account.class))).thenReturn(null);

        // Act
        registrationService.register(USERNAME, EMAIL, PASSWORD);

        // Assert
        verify(userRepository).existsByEmail(EMAIL);
        verify(passwordEncoder).encode(PASSWORD);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo(USERNAME);
        assertThat(capturedUser.getEmail()).isEqualTo(EMAIL);

        verify(accountRepository).save(any(Account.class));
        verifyNoMoreInteractions(userRepository, accountRepository, passwordEncoder);
    }

    @Test
    void register_shouldNormalizeEmail_whenCreatingUser() {
        String unnormalizedEmail = " User@Email.COM   ";
        String normalizedEmail = "user@email.com";

        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);

        User mockUser = User.create(USERNAME, normalizedEmail, PASSWORD_HASH);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(accountRepository.save(any(Account.class))).thenReturn(null);

        // Act
        registrationService.register(USERNAME, unnormalizedEmail, PASSWORD);

        // Assert
        verify(userRepository).existsByEmail(normalizedEmail);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(normalizedEmail);
    }

    /* ---------- register() - Password validation ---------- */
    @Test
    void register_shouldAcceptPassword_whenExactly8Characters() {
        String password = "12345678";

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(PASSWORD_HASH);

        User mockUser = User.create(USERNAME, EMAIL, PASSWORD_HASH);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(accountRepository.save(any(Account.class))).thenReturn(null);

        // Act & Assert
        assertThatCode(() -> registrationService.register(USERNAME, EMAIL, password))
                .doesNotThrowAnyException();

        verify(passwordEncoder).encode(password);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "               ", "short", "1234567"}) // less than 8 characters
    void register_shouldThrow_whenPasswordTooWeak(String password) {
        // Act & Assert
        assertThatThrownBy(() -> registrationService.register(USERNAME, EMAIL, password))
                .isInstanceOf(WeakPasswordException.class);

        verifyNoInteractions(userRepository, accountRepository, passwordEncoder);
    }

    /* ---------- register() - Email validation ---------- */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void register_shouldThrow_whenEmailInvalid(String email) {
        // Act & Assert
        assertThatThrownBy(() -> registrationService.register(USERNAME, email, PASSWORD))
                .isInstanceOf(InvalidEmailException.class);

        verifyNoInteractions(userRepository, accountRepository, passwordEncoder);
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registrationService.register(USERNAME, EMAIL, PASSWORD))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining(EMAIL);

        verify(userRepository).existsByEmail(EMAIL);
        verifyNoInteractions(accountRepository, passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists_caseInsensitive() {
        String existingEmail = "user@email.com";
        String newEmail = "USER@EMAIL.COM";

        when(userRepository.existsByEmail(existingEmail)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> registrationService.register(USERNAME, newEmail, PASSWORD))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository).existsByEmail(existingEmail);
    }

    /* ---------- register() - Username validation ---------- */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void register_shouldThrow_whenUsernameInvalid(String username) {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);

        // Act & Assert
        assertThatThrownBy(() -> registrationService.register(username, EMAIL, PASSWORD))
                .isInstanceOf(InvalidUserFieldException.class)
                .hasMessageContaining("Username");

        verify(userRepository).existsByEmail(EMAIL);
        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(accountRepository);
    }

    /* ---------- register() - Account integrity ---------- */
    @Test
    void register_shouldCreateAccount_withCorrectUser() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);

        User mockSavedUser = User.create(USERNAME, EMAIL, PASSWORD_HASH);
        when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);
        when(accountRepository.save(any(Account.class))).thenReturn(null);

        // Act
        registrationService.register(USERNAME, EMAIL, PASSWORD);

        // Assert
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());

        Account capturedAccount = accountCaptor.getValue();
        assertThat(capturedAccount.getUser()).isEqualTo(mockSavedUser);
        assertThat(capturedAccount.getBalance()).isZero();
    }

    @Test
    void register_shouldNotSaveAccount_whenUserSaveFails() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_HASH);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        assertThatThrownBy(() -> registrationService.register(USERNAME, EMAIL, PASSWORD))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");

        verify(userRepository).save(any(User.class));
        verifyNoInteractions(accountRepository); // Account is not created
    }
}
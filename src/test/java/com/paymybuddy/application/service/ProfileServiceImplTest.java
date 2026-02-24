package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.*;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.exception.InvalidEmailException;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private static final String EMAIL = "user@email.com";
    private static final String NEW_EMAIL = "new@email.com";
    private static final String USERNAME = "oldUsername";
    private static final String NEW_USERNAME = "newUsername";

    // Password constants for changePassword()
    private static final String CURRENT_RAW_PASSWORD = "CurrentPwd123!";
    private static final String NEW_RAW_PASSWORD = "NewPwd123!";
    private static final String ENCODED_CURRENT_PASSWORD = "$2a$10$encoded-current";
    private static final String ENCODED_NEW_PASSWORD = "$2a$10$encoded-new";

    /* ---------- updateProfile() - Happy path ---------- */
    @Test
    void updateProfile_shouldUpdateBothFields_whenDataIsValid() {
        User user = User.create(USERNAME, EMAIL, "password");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);

        profileService.updateProfile("User@email.com", NEW_USERNAME, "New@email.com");

        assertThat(user.getUsername()).isEqualTo(NEW_USERNAME);
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository).existsByEmail(NEW_EMAIL);
        verifyNoMoreInteractions(userRepository);
    }

    /* ---------- create() - Validation errors ---------- */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void updateProfile_shouldThrow_whenCurrentEmailInvalid(String email) {
        assertThatThrownBy(() -> profileService.updateProfile(email, NEW_USERNAME, NEW_EMAIL))
                .isInstanceOf(InvalidEmailException.class);
        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void updateProfile_shouldThrow_whenNewUsernameInvalid(String username) {
        User user = User.create(USERNAME, EMAIL, "password");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> profileService.updateProfile(EMAIL, username, NEW_EMAIL))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findByEmail(EMAIL);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void updateProfile_shouldThrow_whenNewEmailInvalid(String email) {
        assertThatThrownBy(() -> profileService.updateProfile(EMAIL, NEW_USERNAME, email))
                .isInstanceOf(InvalidEmailException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateProfile_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile(EMAIL, NEW_USERNAME, NEW_EMAIL))
                .isInstanceOf(ProfileNotFoundException.class);

        verify(userRepository).findByEmail(EMAIL);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateProfile_shouldNotCheckEmailExistence_whenEmailNotChanged() {
        User user = User.create(USERNAME, EMAIL, "password");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        profileService.updateProfile("User@Email.com", USERNAME, "USER@EMAIL.COM");

        assertThat(user.getUsername()).isEqualTo(USERNAME);
        assertThat(user.getEmail()).isEqualTo(EMAIL);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateProfile_shouldUpdateUsername_whenOnlyUsernameChanges() {
        User user = User.create(USERNAME, EMAIL, "password");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        profileService.updateProfile(EMAIL, NEW_USERNAME, EMAIL);

        assertThat(user.getUsername()).isEqualTo(NEW_USERNAME);
        assertThat(user.getEmail()).isEqualTo(EMAIL);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateProfile_shouldUpdateEmail_whenOnlyEmailChanges_andAvailable() {
        User user = User.create(USERNAME, EMAIL, "password");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);

        profileService.updateProfile(EMAIL, USERNAME, NEW_EMAIL);

        assertThat(user.getUsername()).isEqualTo(USERNAME);
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository).existsByEmail(NEW_EMAIL);
    }

    @Test
    void updateProfile_shouldThrow_whenEmailAlreadyTaken() {
        User user = User.create(USERNAME, EMAIL, "password");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> profileService.updateProfile(EMAIL, USERNAME, NEW_EMAIL))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository).existsByEmail(NEW_EMAIL);
    }

    @Test
    void updateProfile_shouldTrimWhitespace_whenUpdatingUsername() {
        User user = User.create(USERNAME, EMAIL, "password");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        profileService.updateProfile(EMAIL, "  trimmedUsername  ", EMAIL);

        assertThat(user.getUsername()).isEqualTo("trimmedUsername");
    }

    /* ---------- create() - Happy paths ---------- */
    void changePassword_shouldUpdatePassword_whenCurrentPasswordMatches_andNewPasswordValid() {
        User user = User.create(USERNAME, EMAIL, "does-not-matter-here");
        // Ensure the user currently has an ENCODED password stored
        ReflectionTestUtils.setField(user, "password", ENCODED_CURRENT_PASSWORD);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_RAW_PASSWORD, ENCODED_CURRENT_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode(NEW_RAW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

        // Act
        profileService.changePassword("User@Email.com", CURRENT_RAW_PASSWORD, NEW_RAW_PASSWORD);

        // Assert
        assertThat(user.getPasswordHash()).isEqualTo(ENCODED_NEW_PASSWORD);

        verify(userRepository).findByEmail(EMAIL);
        verify(passwordEncoder).matches(CURRENT_RAW_PASSWORD, ENCODED_CURRENT_PASSWORD);
        verify(passwordEncoder).encode(NEW_RAW_PASSWORD);
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    /* ---------- create() - Validation errors ---------- */
    @Test
    void changePassword_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.changePassword(EMAIL, CURRENT_RAW_PASSWORD, NEW_RAW_PASSWORD))
                .isInstanceOf(ProfileNotFoundException.class);

        verify(userRepository).findByEmail(EMAIL);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordDoesNotMatch() {
        User user = User.create(USERNAME, EMAIL, "does-not-matter-here");
        user.changePasswordHash(ENCODED_CURRENT_PASSWORD);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_RAW_PASSWORD, user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> profileService.changePassword(EMAIL, CURRENT_RAW_PASSWORD, NEW_RAW_PASSWORD))
                .isInstanceOf(InvalidCurrentPasswordException.class);

        verify(userRepository).findByEmail(EMAIL);
        verify(passwordEncoder).matches(CURRENT_RAW_PASSWORD, user.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void changePassword_shouldThrow_whenCurrentEmailInvalid(String email) {
        assertThatThrownBy(() -> profileService.changePassword(email, CURRENT_RAW_PASSWORD, NEW_RAW_PASSWORD))
                .isInstanceOf(InvalidEmailException.class);

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "short7"})
    void changePassword_shouldThrow_whenNewPasswordWeak(String newPassword) {
        User user = User.create(USERNAME, EMAIL, "does-not-matter-here");
        user.changePasswordHash(ENCODED_CURRENT_PASSWORD);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_RAW_PASSWORD, user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> profileService.changePassword(EMAIL, CURRENT_RAW_PASSWORD, newPassword))
                .isInstanceOf(WeakPasswordException.class);

        verify(userRepository).findByEmail(EMAIL);
        verify(passwordEncoder).matches(CURRENT_RAW_PASSWORD, user.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePassword_shouldThrow_whenNewPasswordTooLong() {
        User user = User.create(USERNAME, EMAIL, "does-not-matter-here");
        user.changePasswordHash(ENCODED_CURRENT_PASSWORD);

        String tooLongPassword = "a".repeat(71);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_RAW_PASSWORD, user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> profileService.changePassword(EMAIL, CURRENT_RAW_PASSWORD, tooLongPassword))
                .isInstanceOf(TooLongPasswordException.class);

        verify(userRepository).findByEmail(EMAIL);
        verify(passwordEncoder).matches(CURRENT_RAW_PASSWORD, user.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
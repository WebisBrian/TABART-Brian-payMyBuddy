package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.application.service.exception.InvalidProfileUpdateParameterException;
import com.paymybuddy.application.service.exception.UserAccountNotFoundException;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    /* ---------- constants ---------- */
    private static final String EMAIL = "user@email.com";
    private static final String NEW_EMAIL = "new@email.com";
    private static final String USERNAME = "oldUsername";
    private static final String NEW_USERNAME = "newUsername";

    /* ---------- updateProfile() ---------- */
    @Test
    void updateProfile_shouldUpdateUser_whenDataIsValid() {
        User user = User.create(USERNAME, EMAIL, "password");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);

        // Act
        profileService.updateProfile("User@email.com", NEW_USERNAME, "New@email.com");

        // Assert
        assertThat(user.getUsername()).isEqualTo(NEW_USERNAME);
        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository).existsByEmail(NEW_EMAIL);
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);

    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void updateProfile_shouldThrow_whenCurrentEmailInvalid(String email) {
        assertThatThrownBy(() -> profileService.updateProfile(email, NEW_USERNAME, NEW_EMAIL))
                .isInstanceOf(InvalidProfileUpdateParameterException.class);
        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void updateProfile_shouldThrow_whenNewUsernameInvalid(String username) {
        assertThatThrownBy(() -> profileService.updateProfile(EMAIL, username, NEW_EMAIL))
                .isInstanceOf(InvalidProfileUpdateParameterException.class);
        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void updateProfile_shouldThrow_whenNewEmailInvalid(String email) {
        assertThatThrownBy(() -> profileService.updateProfile(EMAIL, NEW_USERNAME, email))
                .isInstanceOf(InvalidProfileUpdateParameterException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateProfile_shouldThrow_whenUserNotFound() {
        String userEmail = EMAIL;

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> profileService.updateProfile(userEmail, NEW_USERNAME, NEW_EMAIL))
                .isInstanceOf(UserAccountNotFoundException.class);

        verify(userRepository).findByEmail(userEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateProfile_shouldNotSave_whenNoChanges_caseInsensitiveEmail() {
        User user = User.create(USERNAME, EMAIL, "password");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        profileService.updateProfile("User@Email.com", USERNAME, "USER@EMAIL.COM");

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldSave_whenOnlyUsernameChanges() {
        User user = User.create(USERNAME, EMAIL, "password");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        profileService.updateProfile(EMAIL, NEW_USERNAME, EMAIL);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldSave_whenOnlyEmailChanges_andAvailable() {
        User user = User.create(USERNAME, EMAIL, "password");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);

        profileService.updateProfile(EMAIL, USERNAME, NEW_EMAIL);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository).existsByEmail(NEW_EMAIL);
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldThrow_whenEmailTaken() {
        User user = User.create(USERNAME, EMAIL, "password");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> profileService.updateProfile(EMAIL, USERNAME, NEW_EMAIL))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository).findByEmail(EMAIL);
        verify(userRepository).existsByEmail(NEW_EMAIL);
        verify(userRepository, never()).save(any());
    }
}
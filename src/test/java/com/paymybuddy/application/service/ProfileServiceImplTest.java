package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    /* ---------- updateProfile() ---------- */
    @Test
    void updateProfile_shouldUpdateUser_whenDataIsValid() {
        User user = User.create("oldUserName", "user@email.com", "password");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);

        // Act
        profileService.updateProfile("User@email.com", "newUserName", "New@email.com");

        // Assert
        assertThat(user.getUserName()).isEqualTo("newUserName");
        assertThat(user.getEmail()).isEqualTo("new@email.com");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_shouldThrow_whenCurrentEmailIsNull() {
        // Act + Assert
        assertThatThrownBy(() -> profileService.updateProfile(null, "newUserName", "newEmail@email.com"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateProfile_shouldThrow_whenUserNotFound() {
        String userEmail = "user@email.com";

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> profileService.updateProfile(userEmail, "newUserName", "New@email.com"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findByEmail(userEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateProfile_shouldThrow_whenEmailIsAlreadyUsed() {
        User user = User.create("user", "user@email.com", "password");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@email.com")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> profileService.updateProfile("User@email.com", "user", "New@email.com"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findByEmail("user@email.com");
        verify(userRepository).existsByEmail("new@email.com");
        verifyNoMoreInteractions(userRepository);
    }
}
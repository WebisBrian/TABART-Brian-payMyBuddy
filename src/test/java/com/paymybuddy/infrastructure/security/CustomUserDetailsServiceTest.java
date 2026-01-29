package com.paymybuddy.infrastructure.security;

import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        String email = "user@email.com";
        User user = User.create("user", email, "password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(result.getUsername()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        String email = "unknown@email.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(userRepository).findByEmail(email);
    }
}
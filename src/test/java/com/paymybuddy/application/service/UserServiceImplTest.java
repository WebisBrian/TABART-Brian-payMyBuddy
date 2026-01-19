package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.entity.UserContact;
import com.paymybuddy.infrastructure.repository.UserContactRepository;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserContactRepository userContactRepository;

    @InjectMocks
    private UserServiceImpl userService;

    /* ---------- addContact() ---------- */
    @Test
    void addContact_shouldCreateContact_whenValidUsers() {
        Long userId = 1L;
        Long contactId = 2L;

        User user = User.create("user", "user@email.com", "password");
        User contact = User.create("contact", "contact@email.com", "password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(contactId)).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Id(userId, contactId)).thenReturn(false);

        when(userContactRepository.save(any(UserContact.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        userService.addContact(userId, contactId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).findById(contactId);
        verify(userContactRepository).existsByUser_IdAndContact_Id(userId, contactId);

        ArgumentCaptor<UserContact> captor = ArgumentCaptor.forClass(UserContact.class);
        verify(userContactRepository).save(captor.capture());

        UserContact saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getContact()).isSameAs(contact);

        verifyNoMoreInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenUserNotFound() {
        Long userId = 1L;
        Long contactId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContact(userId, contactId))
                .isInstanceOf(RuntimeException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findById(contactId);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenContactNotFound() {
        Long userId = 1L;
        Long contactId = 2L;

        User user = User.create("user", "user@email.com", "password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(contactId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContact(userId, contactId))
                .isInstanceOf(RuntimeException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).findById(contactId);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenUserAndContactAreSame() {
        Long sameId = 1L;

        assertThatThrownBy(() -> userService.addContact(sameId, sameId))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenContactAlreadyExists() {
        Long userId = 1L;
        Long contactId = 2L;

        User user = User.create("user", "user@email.com", "password");
        User contact = User.create("contact", "contact@email.com", "password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(contactId)).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Id(userId, contactId)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> userService.addContact(userId, contactId))
                .isInstanceOf(RuntimeException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).findById(contactId);
        verify(userContactRepository).existsByUser_IdAndContact_Id(userId, contactId);

        verify(userContactRepository, never()).save(any(UserContact.class));
        verifyNoMoreInteractions(userRepository, userContactRepository);
    }
}
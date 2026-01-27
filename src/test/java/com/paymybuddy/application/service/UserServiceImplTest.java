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

import java.util.List;
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
        long userId = 1L;
        long contactId = 2L;

        User user = User.create("user", "user@email.com", "password");
        User contact = User.create("contact", "contact@email.com", "password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(contactId)).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Id(userId, contactId)).thenReturn(false);

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
    void addContact_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.addContact(null, 2L))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenContactIdIsNull() {
        assertThatThrownBy(() -> userService.addContact(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenUserNotFound() {
        long userId = 1L;
        long contactId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContact(userId, contactId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findById(contactId);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenContactNotFound() {
        long userId = 1L;
        long contactId = 2L;

        User user = User.create("user", "user@email.com", "password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(contactId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContact(userId, contactId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).findById(contactId);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenUserAndContactAreSame() {
        long sameId = 1L;

        assertThatThrownBy(() -> userService.addContact(sameId, sameId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenContactAlreadyExists() {
        long userId = 1L;
        long contactId = 2L;

        User user = User.create("user", "user@email.com", "password");
        User contact = User.create("contact", "contact@email.com", "password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(contactId)).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Id(userId, contactId)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> userService.addContact(userId, contactId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).findById(contactId);
        verify(userContactRepository).existsByUser_IdAndContact_Id(userId, contactId);

        verify(userContactRepository, never()).save(any(UserContact.class));
        verifyNoMoreInteractions(userRepository, userContactRepository);
    }

    /* ---------- addContactByEmail() ---------- */
    @Test
    void addContactByEmail_shouldCreateContact_whenValidUsers() {
        long userId = 1L;
        String contactEmail = "contact@email.com";

        User user = User.create("user", "user@email.com", "password");
        User contact = User.create("contact", "contact@email.com", "password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(contactEmail)).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Email(userId, contactEmail)).thenReturn(false);

        // Act
        userService.addContactByEmail(userId, contactEmail);

        // Assert
        ArgumentCaptor<UserContact> captor = ArgumentCaptor.forClass(UserContact.class);
        verify(userContactRepository).save(captor.capture());

        UserContact saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getContact()).isSameAs(contact);

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(contactEmail);
        verify(userContactRepository).existsByUser_IdAndContact_Email(userId, contactEmail);
        verifyNoMoreInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.addContactByEmail(null, "contact@email.com"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenContactEmailIsNull() {
        assertThatThrownBy(() -> userService.addContactByEmail(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenUserNotFound() {
        long userId = 1L;
        String contactEmail = "contact@email.com";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContactByEmail(userId, contactEmail))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findByEmail(contactEmail);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenEmailNotFound() {
        long userId = 1L;
        String contactEmail = "contact@email.com";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(contactEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContactByEmail(userId, contactEmail))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(contactEmail);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenUserAndContactAreSame() {
        long userId = 1L;
        String sameEmail = "contact@email.com";

        User user = User.create("user", sameEmail, "password");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.addContactByEmail(userId, sameEmail))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).findByEmail(anyString());
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenContactAlreadyExists() {
        long userId = 1L;
        String contactEmail = "contact@email.com";

        User user = User.create("user", "user@email.com", "password");
        User contact = User.create("contact", contactEmail, "password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(contactEmail)).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Email(userId, contactEmail)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> userService.addContactByEmail(userId, contactEmail))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(contactEmail);
        verify(userContactRepository).existsByUser_IdAndContact_Email(userId, contactEmail);

        verify(userContactRepository, never()).save(any(UserContact.class));
        verifyNoMoreInteractions(userRepository, userContactRepository);
    }


    /* ---------- removeContact() ---------- */
    @Test
    void removeContact_shouldDeleteContact_whenExists() {
        long userId = 1L;
        long contactId = 2L;

        when(userContactRepository.deleteByUser_IdAndContact_Id(userId, contactId)).thenReturn(1L);

        // Act
        userService.removeContact(userId, contactId);

        verify(userContactRepository).deleteByUser_IdAndContact_Id(userId, contactId);
        verifyNoMoreInteractions(userContactRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void removeContact_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.removeContact(null, 2L))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void removeContact_shouldThrow_whenContactIdIsNull() {
        assertThatThrownBy(() -> userService.removeContact(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void removeContact_shouldThrow_whenContactDoesNotExist() {
        long userId = 1L;
        long contactId = 2L;

        when(userContactRepository.deleteByUser_IdAndContact_Id(userId, contactId)).thenReturn(0L);

        assertThatThrownBy(() -> userService.removeContact(userId, contactId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userContactRepository).deleteByUser_IdAndContact_Id(userId, contactId);
        verifyNoMoreInteractions(userContactRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void removeContact_shouldThrow_whenUserAndContactAreSame() {
        long sameId = 1L;

        assertThatThrownBy(() -> userService.removeContact(sameId, sameId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    /* ---------- listContacts() ---------- */
    @Test
    void listContacts_shouldReturnContactUsers() {
        long userId = 1L;

        User user = User.create("user", "user@email.com", "pwd");
        User contact1 = User.create("contact1", "contact1@email.com", "pwd");
        User contact2 = User.create("contact2", "contact2@email.com", "pwd");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userContactRepository.findContactsByUserId(userId)).thenReturn(List.of(contact1, contact2));

        // Act
        List<User> result = userService.listContacts(userId);

        // Assert
        assertThat(result).containsExactly(contact1, contact2);

        verify(userRepository).findById(userId);
        verify(userContactRepository).findContactsByUserId(userId);
        verifyNoMoreInteractions(userRepository, userContactRepository);
    }

    @Test
    void listContacts_shouldReturnEmptyList_whenNoContacts() {
        long userId = 1L;
        User user = User.create("user", "user@email.com", "pwd");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userContactRepository.findContactsByUserId(userId)).thenReturn(List.of());

        // Act
        List<User> result = userService.listContacts(userId);

        assertThat(result).isEmpty();

        verify(userRepository).findById(userId);
        verify(userContactRepository).findContactsByUserId(userId);
        verifyNoMoreInteractions(userRepository, userContactRepository);
    }

    @Test
    void listContacts_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.listContacts(null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void listContacts_shouldThrow_whenUserNotFound() {
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.listContacts(userId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository).findById(userId);
        verifyNoInteractions(userContactRepository);
        verifyNoMoreInteractions(userRepository);
    }
}
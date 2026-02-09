package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.ContactAlreadyExistsException;
import com.paymybuddy.application.service.exception.ContactNotFoundException;
import com.paymybuddy.application.service.exception.UserNotFoundException;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.entity.UserContact;
import com.paymybuddy.domain.exception.InvalidEmailException;
import com.paymybuddy.domain.exception.SelfContactNotAllowedException;
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

    /* ---------- getByEmail() - Happy path ---------- */
    @Test
    void getByEmail_shouldReturnUser_whenValidEmail() {
        String email = "user@email.com";
        User user = User.create("user", email, "password");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getByEmail(email);

        assertThat(result).isSameAs(user);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getByEmail_shouldNormalizeEmail() {
        String unnormalizedEmail = "  USER@Email.COM  ";
        String normalizedEmail = "user@email.com";
        User user = User.create("user", normalizedEmail, "password");

        when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));

        User result = userService.getByEmail(unnormalizedEmail);

        assertThat(result).isSameAs(user);
        verify(userRepository).findByEmail(normalizedEmail);
    }

    /* ---------- getByEmail() - Validation errors ---------- */
    @Test
    void getByEmail_shouldThrow_whenEmailInvalid() {
        assertThatThrownBy(() -> userService.getByEmail(null))
                .isInstanceOf(InvalidEmailException.class);

        assertThatThrownBy(() -> userService.getByEmail("  "))
                .isInstanceOf(InvalidEmailException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void getByEmail_shouldThrow_whenUserNotFound() {
        String email = "user@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(email);

        verify(userRepository).findByEmail(email);
    }

    /* ---------- addContact() - Happy Path ---------- */
    @Test
    void addContact_shouldCreateContact_whenValid() {
        User user = User.create("user", "user@email.com", "pwd");
        User contact = User.create("contact", "contact@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Id(1L, 2L)).thenReturn(false);

        userService.addContact(1L, 2L);

        ArgumentCaptor<UserContact> captor = ArgumentCaptor.forClass(UserContact.class);
        verify(userContactRepository).save(captor.capture());

        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getContact()).isSameAs(contact);
    }

    /* ---------- addContact() - Validation errors ---------- */
    @Test
    void addContact_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.addContact(null, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null");

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenContactIdIsNull() {
        assertThatThrownBy(() -> userService.addContact(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contact ID must not be null");

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContact(1L, 2L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenContactNotFound() {
        User user = User.create("user", "user@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContact(1L, 2L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContact_shouldThrow_whenSelfContact() {
        User user = User.create("user", "user@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userContactRepository.existsByUser_IdAndContact_Id(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> userService.addContact(1L, 1L))
                .isInstanceOf(SelfContactNotAllowedException.class);

        verify(userRepository, times(2)).findById(1L); // Appelé 2 fois (user + contact)
    }

    @Test
    void addContact_shouldThrow_whenContactAlreadyExists() {
        User user = User.create("user", "user@email.com", "pwd");
        User contact = User.create("contact", "contact@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Id(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> userService.addContact(1L, 2L))
                .isInstanceOf(ContactAlreadyExistsException.class);

        verify(userContactRepository, never()).save(any());
    }

    /* ---------- addContactByEmail() - Happy Path ---------- */
    @Test
    void addContactByEmail_shouldCreateContact_whenValid() {
        User user = User.create("user", "user@email.com", "pwd");
        User contact = User.create("contact", "contact@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("contact@email.com")).thenReturn(Optional.of(contact));
        when(userContactRepository.existsByUser_IdAndContact_Email(1L, "contact@email.com"))
                .thenReturn(false);

        userService.addContactByEmail(1L, "contact@email.com");

        ArgumentCaptor<UserContact> captor = ArgumentCaptor.forClass(UserContact.class);
        verify(userContactRepository).save(captor.capture());

        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getContact()).isSameAs(contact);
    }

    /* ---------- addContactByEmail() - Validation errors ---------- */
    @Test
    void addContactByEmail_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.addContactByEmail(null, "contact@email.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null");

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenEmailInvalid() {
        assertThatThrownBy(() -> userService.addContactByEmail(1L, null))
                .isInstanceOf(InvalidEmailException.class);

        assertThatThrownBy(() -> userService.addContactByEmail(1L, "  "))
                .isInstanceOf(InvalidEmailException.class);

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContactByEmail(1L, "contact@email.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verifyNoInteractions(userContactRepository);
    }

    @Test
    void addContactByEmail_shouldThrow_whenContactNotFound() {
        User user = User.create("user", "user@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userContactRepository.existsByUser_IdAndContact_Email(1L, "contact@email.com"))
                .thenReturn(false);
        when(userRepository.findByEmail("contact@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addContactByEmail(1L, "contact@email.com"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByEmail("contact@email.com");
    }

    @Test
    void addContactByEmail_shouldThrow_whenSelfContact() {
        String email = "user@email.com";
        User user = User.create("user", email, "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userContactRepository.existsByUser_IdAndContact_Email(1L, email)).thenReturn(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.addContactByEmail(1L, email))
                .isInstanceOf(SelfContactNotAllowedException.class);

        verify(userContactRepository, never()).save(any());
    }

    @Test
    void addContactByEmail_shouldThrow_whenContactAlreadyExists() {
        User user = User.create("user", "user@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userContactRepository.existsByUser_IdAndContact_Email(1L, "contact@email.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.addContactByEmail(1L, "contact@email.com"))
                .isInstanceOf(ContactAlreadyExistsException.class);

        verify(userRepository, never()).findByEmail(anyString());
        verify(userContactRepository, never()).save(any());
    }

    /* ---------- removeContact() - Happy Path ---------- */
    @Test
    void removeContact_shouldDeleteContact_whenExists() {
        when(userContactRepository.deleteByUser_IdAndContact_Id(1L, 2L)).thenReturn(1L);

        userService.removeContact(1L, 2L);

        verify(userContactRepository).deleteByUser_IdAndContact_Id(1L, 2L);
    }

    /* ---------- removeContact() - Validation errors ---------- */
    @Test
    void removeContact_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.removeContact(null, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null");

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void removeContact_shouldThrow_whenContactIdIsNull() {
        assertThatThrownBy(() -> userService.removeContact(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Contact ID must not be null");

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void removeContact_shouldThrow_whenContactNotFound() {
        when(userContactRepository.deleteByUser_IdAndContact_Id(1L, 2L)).thenReturn(0L);

        assertThatThrownBy(() -> userService.removeContact(1L, 2L))
                .isInstanceOf(ContactNotFoundException.class);
    }

    /* ---------- listContacts() - Happy Path ---------- */
    @Test
    void listContacts_shouldReturnContactUsers() {
        User user = User.create("user", "user@email.com", "pwd");
        User contact1 = User.create("contact1", "contact1@email.com", "pwd");
        User contact2 = User.create("contact2", "contact2@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userContactRepository.findContactsByUserId(1L)).thenReturn(List.of(contact1, contact2));

        List<User> result = userService.listContacts(1L);

        assertThat(result).containsExactly(contact1, contact2);
    }

    @Test
    void listContacts_shouldReturnEmptyList_whenNoContacts() {
        User user = User.create("user", "user@email.com", "pwd");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userContactRepository.findContactsByUserId(1L)).thenReturn(List.of());

        List<User> result = userService.listContacts(1L);

        assertThat(result).isEmpty();
    }

    /* ---------- listContacts() - Validation errors ---------- */
    @Test
    void listContacts_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.listContacts(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID must not be null");

        verifyNoInteractions(userRepository, userContactRepository);
    }

    @Test
    void listContacts_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.listContacts(1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(1L);
        verifyNoInteractions(userContactRepository);
    }
}
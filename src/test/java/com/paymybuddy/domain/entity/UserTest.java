package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.InvalidUserFieldException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    /* ---------- create() ---------- */
    @Test
    void create_shouldCreateUser_whenValid() {
        User user = User.create("User 1", "user@email.com", "password");

        assertNotNull(user);
        assertEquals("User 1", user.getUsername());
        assertEquals("user@email.com", user.getEmail());
    }

    @Test
    void create_shouldTrimUsernameAndEmail() {
        User user = User.create("  User 1  ", "  User@Email.com  ", "password");

        assertEquals("User 1", user.getUsername());
        assertEquals("user@email.com", user.getEmail());
    }

    @Test
    void create_shouldThrow_whenUsernameIsNull() {
        InvalidUserFieldException ex = assertThrows(InvalidUserFieldException.class,
                () -> User.create(null, "User@email.com", "password"));

        assertTrue(ex.getMessage().contains("Username must not be null or blank."));
    }

    @Test
    void create_shouldThrow_whenEmailIsBlank() {
        InvalidUserFieldException ex = assertThrows(InvalidUserFieldException.class,
                () -> User.create("User 1", " ", "password"));

        assertTrue(ex.getMessage().contains("Email must not be null or blank."));
    }

    /* ---------- changeUsername() ---------- */
    @Test
    void changeUsername_shouldTrim() {
        User user = User.create("User 1", "User@email.com", "hash");

        user.changeUsername("  New Name  ");

        assertEquals("New Name", user.getUsername());
    }

    /* ---------- changeEmail() ---------- */
    @Test
    void changeEmail_shouldNormalizeAndTrim() {
        User user = User.create("User 1", "User@email.com", "hash");

        user.changeEmail("  NEW@Email.COM  ");

        assertEquals("new@email.com", user.getEmail());
    }

    /* ---------- changePasswordHash() ---------- */
    @Test
    void changePasswordHash_shouldOnlyChangePasswordHash_notUsername() {
        User user = User.create("User 1", "User@email.com", "hash");

        user.changePasswordHash("newHash");

        assertEquals("User 1", user.getUsername());
    }
}
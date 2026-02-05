package com.paymybuddy.domain.entity;

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
        assertThrows(IllegalArgumentException.class, () -> User.create(null, "User@email.com", "password"));
    }

    @Test
    void create_shouldThrow_whenEmailIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> User.create("User 1", " ", "password"));
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
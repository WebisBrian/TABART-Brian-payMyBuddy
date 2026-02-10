package com.paymybuddy.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    /* ---------- create() - Happy path ---------- */
    @Test
    void create_shouldCreateUser_whenAllFieldsValid() {
        User user = User.create("User 1", "user@email.com", "password");

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("User 1");
        assertThat(user.getEmail()).isEqualTo("user@email.com");
    }

    @Test
    void create_shouldTrimAndNormalizeFields() {
        User user = User.create("  User 1  ", "  User@Email.COM  ", "  password  ");

        assertThat(user.getUsername()).isEqualTo("User 1");
        assertThat(user.getEmail()).isEqualTo("user@email.com");
    }

    /* ---------- create() - Validation errors ---------- */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void create_shouldThrow_whenUsernameInvalid(String username) {
        assertThatThrownBy(() -> User.create(username, "user@email.com", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must not be null or blank");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void create_shouldThrow_whenEmailInvalid(String email) {
        assertThatThrownBy(() -> User.create("User 1", email, "password"))
                .hasMessageContaining("must not be null or blank");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void create_shouldThrow_whenPasswordInvalid(String password) {
        assertThatThrownBy(() -> User.create("User 1", "user@email.com", password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must not be null or blank");
    }

    /* ---------- changeUsername() ---------- */
    @Test
    void changeUsername_shouldUpdateAndTrim() {
        User user = User.create("User 1", "user@email.com", "hash");

        user.changeUsername("  New Name  ");

        assertThat(user.getUsername()).isEqualTo("New Name");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void changeUsername_shouldThrow_whenInvalid(String username) {
        User user = User.create("User 1", "user@email.com", "hash");

        assertThatThrownBy(() -> user.changeUsername(username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must not be null or blank");
    }

    /* ---------- changeEmail() ---------- */
    @Test
    void changeEmail_shouldNormalizeAndTrim() {
        User user = User.create("User 1", "user@email.com", "hash");

        user.changeEmail("  NEW@Email.COM  ");

        assertThat(user.getEmail()).isEqualTo("new@email.com");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void changeEmail_shouldThrow_whenInvalid(String email) {
        User user = User.create("User 1", "user@email.com", "hash");

        assertThatThrownBy(() -> user.changeEmail(email))
                .hasMessageContaining("must not be null or blank");
    }

    /* ---------- changePasswordHash() ---------- */
    @Test
    void changePasswordHash_shouldUpdatePassword() {
        User user = User.create("User 1", "user@email.com", "oldHash");

        user.changePasswordHash("  newHash  ");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void changePasswordHash_shouldThrow_whenInvalid(String password) {
        User user = User.create("User 1", "user@email.com", "hash");

        assertThatThrownBy(() -> user.changePasswordHash(password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must not be null or blank");
    }

    /* ---------- equals() and hashCode() ---------- */
    @Test
    void equals_shouldBeBasedOnId() {
        User user1 = User.create("User 1", "user1@email.com", "hash");
        User user2 = User.create("User 2", "user2@email.com", "hash");

        ReflectionTestUtils.setField(user1, "id", 1L);
        ReflectionTestUtils.setField(user2, "id", 1L);

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void equals_shouldBeDifferent_whenDifferentIds() {
        User user1 = User.create("User 1", "user@email.com", "hash");
        User user2 = User.create("User 1", "user@email.com", "hash");

        ReflectionTestUtils.setField(user1, "id", 1L);
        ReflectionTestUtils.setField(user2, "id", 2L);

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void equals_shouldBeFalse_whenComparedToNull() {
        User user = User.create("User 1", "user@email.com", "hash");

        assertThat(user).isNotEqualTo(null);
    }
}
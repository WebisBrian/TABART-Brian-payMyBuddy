package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.MissingUserOrContactException;
import com.paymybuddy.domain.exception.SelfContactNotAllowedException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UserContactTest {

    /* ---------- create() ---------- */
    @Test
    void create_shouldCreateUserContact_whenValid() {
        User user = validUser("user@mail.com");
        User contact = validUser("contact@mail.com");

        UserContact userContact = UserContact.create(user, contact);

        assertNotNull(userContact);
        assertEquals(user, userContact.getUser());
        assertEquals(contact, userContact.getContact());
    }

    @Test
    void create_shouldThrow_whenUserIsNull() {
        User contact = validUser("contact@mail.com");

        MissingUserOrContactException ex = assertThrows(MissingUserOrContactException.class, () ->
                UserContact.create(null, contact)
        );

        assertTrue(ex.getMessage().contains("User or contact must not be null"));
    }

    @Test
    void create_shouldThrow_whenContactIsNull() {
        User user = validUser("user@mail.com");

        MissingUserOrContactException ex = assertThrows(MissingUserOrContactException.class, () ->
                UserContact.create(user, null)
        );

        assertTrue(ex.getMessage().contains("User or contact must not be null"));
    }

    @Test
    void create_shouldThrow_whenUserAndContactAreSameInstance() {
        User user = validUser("user@mail.com");

        SelfContactNotAllowedException ex = assertThrows(SelfContactNotAllowedException.class, () ->
                UserContact.create(user, user)
        );

        assertTrue(ex.getMessage().contains("User cannot add himself as a contact."));
    }

    @Test
    void create_shouldThrow_whenUserAndContactHaveSameId() {
        User user = validUser("user@mail.com");
        User contact = validUser("contact@mail.com");

        // Simulate "same user" coming from DB with different instances but the same id
        setId(user, 42L);
        setId(contact, 42L);

        SelfContactNotAllowedException ex = assertThrows(SelfContactNotAllowedException.class, () ->
                UserContact.create(user, contact)
        );

        assertTrue(ex.getMessage().contains("User cannot add himself as a contact."));
    }

    /* ---------- Helpers ---------- */
    private static User validUser(String email) {
        return User.create("User", email, "hash");
    }

    /**
     * Test-only helper: sets the private field "id" using reflection.
     * Use in unit tests to simulate persisted entities.
     */
    private static void setId(User user, Long id) {
        try {
            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set User.id in test", e);
        }
    }
}

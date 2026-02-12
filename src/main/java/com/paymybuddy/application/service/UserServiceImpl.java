package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.ContactAlreadyExistsException;
import com.paymybuddy.application.service.exception.ContactNotFoundException;
import com.paymybuddy.application.service.exception.UserNotFoundException;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.entity.UserContact;
import com.paymybuddy.domain.utils.EmailNormalizer;
import com.paymybuddy.infrastructure.repository.UserContactRepository;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;

    public UserServiceImpl(UserRepository userRepository, UserContactRepository userContactRepository) {
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
    }

    /* ---------- getByEmail() ---------- */
    @Override
    @Transactional (readOnly = true)
    public User getByEmail(String email) {
        String normalizedEmail = EmailNormalizer.normalize(email);

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    /* ---------- addContact() ---------- */
    @Override
    @Transactional
    public void addContact(Long userId, Long contactId) {
        requireNonNull(userId, "User ID must not be null");
        requireNonNull(contactId, "Contact ID must not be null");

        User user = findUserById(userId);
        User contact = findUserById(contactId);

        if (userContactRepository.existsByUser_IdAndContact_Id(userId, contactId)) {
            throw new ContactAlreadyExistsException(userId, contactId);
        }

        UserContact newContact = UserContact.create(user, contact);
        userContactRepository.save(newContact);
    }

    /* ---------- addContactByEmail() ---------- */
    @Override
    @Transactional
    public void addContactByEmail(Long userId, String contactEmail) {
        requireNonNull(userId, "User ID must not be null");
        String normalizedEmail = EmailNormalizer.normalize(contactEmail);

        User user = findUserById(userId);

        if (userContactRepository.existsByUser_IdAndContact_Email(userId, normalizedEmail)) {
            throw new ContactAlreadyExistsException(userId, normalizedEmail);
        }

        User contact = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("Contact not found with email: " + normalizedEmail));

        UserContact newContact = UserContact.create(user, contact);
        userContactRepository.save(newContact);
    }

    /* ---------- removeContact() ---------- */
    @Override
    @Transactional
    public void removeContact(Long userId, Long contactId) {
        requireNonNull(userId, "User ID must not be null");
        requireNonNull(contactId, "Contact ID must not be null");

        long deleted = userContactRepository.deleteByUser_IdAndContact_Id(userId, contactId);

        if (deleted == 0) {
            throw new ContactNotFoundException();
        }
    }

    /* ---------- listContacts() ---------- */
    @Override
    @Transactional(readOnly = true)
    public List<User> listContacts(Long userId) {
        requireNonNull(userId, "User ID must not be null");

        findUserById(userId);

        return userContactRepository.findContactsByUserId(userId);
    }

    /* ---------- Helpers ---------- */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
}

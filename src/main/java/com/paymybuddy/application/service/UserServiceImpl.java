package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.ContactAlreadyExistsException;
import com.paymybuddy.application.service.exception.ContactNotFoundException;
import com.paymybuddy.application.service.exception.UserNotFoundException;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.entity.UserContact;
import com.paymybuddy.domain.utils.EmailNormalizer;
import com.paymybuddy.infrastructure.repository.UserContactRepository;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.paymybuddy.common.logging.SensitiveDataMasker.maskEmail;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;

    public UserServiceImpl(UserRepository userRepository, UserContactRepository userContactRepository) {
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
    }

    @Override
    @Transactional (readOnly = true)
    public User getByEmail(String email) {
        logger.debug("getByEmail called: email={}", maskEmail(email));

        String normalizedEmail = EmailNormalizer.normalize(email);

        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    logger.warn("User not found by email: email={}", maskEmail(normalizedEmail));
                    return new UserNotFoundException(email);
                });
    }

    @Override
    @Transactional
    public void addContact(Long userId, Long contactId) {
        logger.debug("addContact called: userId={}, contactId={}", userId, contactId);

        requireNonNull(userId, "User ID must not be null");
        requireNonNull(contactId, "Contact ID must not be null");

        User user = findUserById(userId);
        User contact = findUserById(contactId);

        if (userContactRepository.existsByUser_IdAndContact_Id(userId, contactId)) {
            logger.warn("Add contact refused: already exists. userId={}, contactId={}", userId, contactId);
            throw new ContactAlreadyExistsException(userId, contactId);
        }

        UserContact newContact = UserContact.create(user, contact);
        userContactRepository.save(newContact);

        logger.info("Contact added: userId={}, contactId={}", userId, contactId);
    }

    @Override
    @Transactional
    public void addContactByEmail(Long userId, String contactEmail) {
        logger.debug("addContactByEmail called: userId={}, contactEmail={}", userId, maskEmail(contactEmail));

        requireNonNull(userId, "User ID must not be null");
        String normalizedEmail = EmailNormalizer.normalize(contactEmail);

        User user = findUserById(userId);

        if (userContactRepository.existsByUser_IdAndContact_Email(userId, normalizedEmail)) {
            logger.warn("Add contact refused: already exists. userId={}, contactEmail={}",
                    userId, maskEmail(normalizedEmail));
            throw new ContactAlreadyExistsException(userId, normalizedEmail);
        }

        User contact = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    logger.warn("Add contact refused: contact not found by email. userId={}, contactEmail={}",
                            userId, maskEmail(normalizedEmail));
                    return new UserNotFoundException("Contact not found with email: " + normalizedEmail);
                });

        UserContact newContact = UserContact.create(user, contact);
        userContactRepository.save(newContact);

        logger.info("Contact added by email: userId={}, contactId={}, contactEmail={}",
                userId, contact.getId(), maskEmail(normalizedEmail));
    }

    @Override
    @Transactional
    public void removeContact(Long userId, Long contactId) {
        logger.debug("removeContact called: userId={}, contactId={}", userId, contactId);

        requireNonNull(userId, "User ID must not be null");
        requireNonNull(contactId, "Contact ID must not be null");

        long deleted = userContactRepository.deleteByUser_IdAndContact_Id(userId, contactId);

        if (deleted == 0) {
            logger.warn("Remove contact refused: relation not found. userId={}, contactId={}", userId, contactId);
            throw new ContactNotFoundException();
        }

        logger.info("Contact removed: userId={}, contactId={}", userId, contactId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listContacts(Long userId) {
        logger.debug("listContacts called: userId={}", userId);

        requireNonNull(userId, "User ID must not be null");
        findUserById(userId);

        List<User> contacts = userContactRepository.findContactsByUserId(userId);

        logger.debug("Contacts listed: userId={}, count={}", userId, contacts.size());

        return contacts;
    }

    /* ---------- Helpers ---------- */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found by id: userId={}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            logger.warn("Invalid argument: {}", message);
            throw new IllegalArgumentException(message);
        }
    }
}

package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.entity.UserContact;
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

    @Override
    @Transactional
    public void addContact(Long userId, Long contactId) {
        if (userId == null || contactId == null) {
            throw new IllegalArgumentException("User and contact IDs must not be null.");
        }

        if (userId.equals(contactId)) {
            throw new IllegalArgumentException("Cannot add self as contact");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        User contact = userRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found."));

        if (userContactRepository.existsByUser_IdAndContact_Id(userId, contactId)) {
            throw new IllegalArgumentException("User already has this contact.");
        }

        UserContact newContact = UserContact.create(user, contact);
        userContactRepository.save(newContact);
    }

    @Override
    @Transactional
    public void addContactByEmail(Long userId, String contactEmail) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }

        if (contactEmail == null) {
            throw new IllegalArgumentException("Contact email must not be null.");
        }

        String normalizedEmail = contactEmail.trim();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getEmail().trim().equalsIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Cannot add self as contact.");
        }

        if(userContactRepository.existsByUser_IdAndContact_Email(userId, normalizedEmail)) {
            throw new IllegalArgumentException("User already has this contact.");
        }

        User contact = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found."));

        UserContact newContact = UserContact.create(user, contact);
        userContactRepository.save(newContact);
    }

    @Override
    @Transactional
    public void removeContact(Long userId, Long contactId) {
        if (userId == null || contactId == null) {
            throw new IllegalArgumentException("User and contact IDs must not be null.");
        }

        if (userId.equals(contactId)) {
            throw new IllegalArgumentException("Cannot remove self as contact.");
        }

        long deleted = userContactRepository.deleteByUser_IdAndContact_Id(userId, contactId);
        if (deleted == 0) {
            throw new IllegalArgumentException("User does not have this contact.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listContacts(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null.");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return userContactRepository.findContactsByUserId(userId);
    }
}

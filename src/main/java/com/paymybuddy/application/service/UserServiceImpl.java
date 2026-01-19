package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.entity.UserContact;
import com.paymybuddy.infrastructure.repository.UserContactRepository;
import com.paymybuddy.infrastructure.repository.UserRepository;

import java.util.List;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserContactRepository userContactRepository;

    public UserServiceImpl(UserRepository userRepository, UserContactRepository userContactRepository) {
        this.userRepository = userRepository;
        this.userContactRepository = userContactRepository;
    }

    @Override
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

    public void removeContact(Long userId, Long contactId) {
    }

    public List<User> listContacts(Long userId) {
        return List.of();
    }
}

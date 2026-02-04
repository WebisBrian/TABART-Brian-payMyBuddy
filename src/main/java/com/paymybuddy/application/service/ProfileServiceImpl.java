package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    public ProfileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void updateProfile(String email, String newUsername, String newEmail) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank.");
        }
        if (newUsername == null || newUsername.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or blank.");
        }
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("New email must not be null or blank.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedNewEmail = newEmail.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!normalizedEmail.equals(normalizedNewEmail) && userRepository.existsByEmail(normalizedNewEmail)) {
            throw new IllegalArgumentException("New email is not available.");
        }

        boolean changed = false;

        if (!user.getUsername().equals(newUsername)) {
            user.changeUsername(newUsername);
            changed = true;
        }

        if (!normalizedEmail.equals(normalizedNewEmail)) {
            user.changeEmail(normalizedNewEmail);
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
}

package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.application.service.exception.UserAccountNotFoundException;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.utils.EmailNormalizer;
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
    public void updateProfile(String currentEmail, String newUsername, String newEmail) {

        String currentEmailNormalized = EmailNormalizer.normalize(currentEmail);
        String newEmailNormalized  = EmailNormalizer.normalize(newEmail);

        User user = getUserByNormalizedEmail(currentEmailNormalized);

        boolean emailChanged = !currentEmailNormalized.equals(newEmailNormalized);
        // DB-level rule: unique email
        if (emailChanged && userRepository.existsByEmail(newEmailNormalized)) {
            throw new EmailAlreadyUsedException(newEmailNormalized);
        }

        user.changeUsername(newUsername);
        if (emailChanged) {
            user.changeEmail(newEmail);
        }

    }

    private User getUserByNormalizedEmail(String normalizedEmail) {
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserAccountNotFoundException(normalizedEmail));
    }
}

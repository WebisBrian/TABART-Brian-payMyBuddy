package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.*;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.utils.EmailNormalizer;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.paymybuddy.common.logging.SensitiveDataMasker.maskEmail;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public ProfileServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void updateProfile(String currentEmail, String newUsername, String newEmail) {
        logger.debug("updateProfile called: currentEmail={}, newUsernamePresent={}, newEmail={}",
                maskEmail(currentEmail),
                newUsername != null && !newUsername.isBlank(),
                maskEmail(newEmail));

        String currentEmailNormalized = EmailNormalizer.normalize(currentEmail);
        String newEmailNormalized = EmailNormalizer.normalize(newEmail);

        User user = getUserByNormalizedEmail(currentEmailNormalized);

        boolean emailChanged = !currentEmailNormalized.equals(newEmailNormalized);
        logger.debug("Profile update: emailChanged={}", emailChanged);

        // DB-level rule: unique email
        if (emailChanged && userRepository.existsByEmail(newEmailNormalized)) {
            logger.warn("Profile update refused: email already used. currentEmail={}, newEmail={}",
                    maskEmail(currentEmailNormalized), maskEmail(newEmailNormalized));
            throw new EmailAlreadyUsedException(newEmailNormalized);
        }

        user.changeUsername(newUsername);

        if (emailChanged) {
            user.changeEmail(newEmail);
        }

        logger.info("Profile updated: userId={}, usernameChanged={}, emailChanged={}, emailFrom={}, emailTo={}",
                user.getId(),
                true,
                emailChanged,
                maskEmail(currentEmailNormalized),
                emailChanged ? maskEmail(newEmailNormalized) : "-");

    }

    @Override
    @Transactional
    public void changePassword(String currentEmail, String currentPassword, String newPassword) {
        logger.debug("changePassword called: currentEmail={}", maskEmail(currentEmail));

        String currentEmailNormalized = EmailNormalizer.normalize(currentEmail);
        User user = getUserByNormalizedEmail(currentEmailNormalized);

        // Verify the current password against a stored encoded password
        boolean matches = passwordEncoder.matches(currentPassword, user.getPasswordHash());
        if (!matches) {
            logger.warn("Password change refused: invalid current password. userId={}", user.getId());
            throw new InvalidCurrentPasswordException();
        }

        validatePasswordComplexity(newPassword);

        String encoded = passwordEncoder.encode(newPassword);
        user.changePasswordHash(encoded);

        logger.info("Password changed successfully: userId={}", user.getId());
    }

    /* ---------- Helpers ---------- */
    private User getUserByNormalizedEmail(String normalizedEmail) {
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    logger.warn("Profile not found for email={}", maskEmail(normalizedEmail));
                    return new ProfileNotFoundException(normalizedEmail);
                });
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || password.isEmpty()) {
            logger.warn("Update refused: weak password (null/empty)");
            throw new WeakPasswordException("Password must not be null or empty.");
        }

        if (!password.equals(password.trim())) {
            logger.warn("Update refused: weak password (leading/trailing spaces)");
            throw new WeakPasswordException("Password must not contain leading/trailing spaces");
        }

        if (password.length() < 8) {
            logger.warn("Update refused: weak password (too short)");
            throw new WeakPasswordException("Password must be at least 8 characters long.");
        }

        if (password.length() > 70) {
            logger.warn("Update refused: password too long");
            throw new TooLongPasswordException();
        }
    }
}

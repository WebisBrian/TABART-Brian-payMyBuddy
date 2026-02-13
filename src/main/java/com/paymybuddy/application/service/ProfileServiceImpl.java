package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.application.service.exception.ProfileNotFoundException;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.utils.EmailNormalizer;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.paymybuddy.common.logging.SensitiveDataMasker.maskEmail;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private final UserRepository userRepository;

    public ProfileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    /* ---------- Helpers ---------- */
    private User getUserByNormalizedEmail(String normalizedEmail) {
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    logger.warn("Profile not found for email={}", maskEmail(normalizedEmail));
                    return new ProfileNotFoundException(normalizedEmail);
                });
    }
}

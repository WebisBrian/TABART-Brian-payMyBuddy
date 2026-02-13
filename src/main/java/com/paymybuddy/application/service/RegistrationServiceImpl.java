package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.application.service.exception.TooLongPasswordException;
import com.paymybuddy.application.service.exception.WeakPasswordException;
import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.utils.EmailNormalizer;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.paymybuddy.common.logging.SensitiveDataMasker.maskEmail;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationServiceImpl(UserRepository userRepository,
                                   AccountRepository accountRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(String username, String email, String password) {
        logger.debug("register called: usernamePresent={}, email={}",
                username != null && !username.isBlank(),
                maskEmail(email));

        validatePasswordComplexity(password);

        String normalizedEmail = EmailNormalizer.normalize(email);

        if (userRepository.existsByEmail(normalizedEmail)) {
            logger.warn("Registration refused: email already used. email={}", maskEmail(normalizedEmail));
            throw new EmailAlreadyUsedException(normalizedEmail);
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = User.create(username, normalizedEmail, passwordHash);
        User savedUser = userRepository.save(user);

        Account account = Account.create(savedUser);
        accountRepository.save(account);

        logger.info("Registration completed: userId={}, email={}", savedUser.getId(), maskEmail(normalizedEmail));
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || password.isEmpty()) {
            logger.warn("Registration refused: weak password (null/empty)");
            throw new WeakPasswordException("Password must not be null or empty.");
        }

        if (!password.equals(password.trim())) {
            logger.warn("Registration refused: weak password (leading/trailing spaces)");
            throw new WeakPasswordException("Password must not contain leading/trailing spaces");
        }

        if (password.length() < 8) {
            logger.warn("Registration refused: weak password (too short)");
            throw new WeakPasswordException("Password must be at least 8 characters long.");
        }

        if (password.length() > 70) {
            logger.warn("Registration refused: password too long");
            throw new TooLongPasswordException();
        }
    }
}

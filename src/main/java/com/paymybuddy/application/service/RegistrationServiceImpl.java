package com.paymybuddy.application.service;

import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.application.service.exception.WeakPasswordException;
import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.utils.EmailNormalizer;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import com.paymybuddy.infrastructure.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationServiceImpl implements RegistrationService {

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
        validatePasswordComplexity(password);

        String normalizedEmail = EmailNormalizer.normalize(email);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyUsedException(normalizedEmail);
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = User.create(username, normalizedEmail, passwordHash);
        User savedUser = userRepository.save(user);

        Account account = Account.create(savedUser);
        accountRepository.save(account);
    }

    private void validatePasswordComplexity(String password) {
        if (password == null || password.isEmpty()) {
            throw new WeakPasswordException("Password must not be null or empty.");
        }

        if (!password.equals(password.trim())) {
            throw new WeakPasswordException("Password must not contain leading/trailing spaces");
        }

        if (password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters long.");
        }
    }
}

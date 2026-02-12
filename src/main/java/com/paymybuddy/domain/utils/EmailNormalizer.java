package com.paymybuddy.domain.utils;

import com.paymybuddy.domain.exception.InvalidEmailException;

import java.util.regex.Pattern;

public class EmailNormalizer {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static String normalize(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidEmailException("Email must not be null or blank.");
        }

        String normalized = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new InvalidEmailException("Invalid email format: " + normalized + ".");
        }

        return normalized;
    }
}
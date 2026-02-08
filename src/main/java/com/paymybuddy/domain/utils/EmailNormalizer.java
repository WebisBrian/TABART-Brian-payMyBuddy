package com.paymybuddy.domain.utils;

import com.paymybuddy.domain.exception.InvalidEmailException;

public class EmailNormalizer {

    /**
     * Normalizes an email address.
     * The returned value is never null.
     * The returned value is always lowercase. */
    public static String normalize(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidEmailException();
        }
        return email.trim().toLowerCase();
    }
}

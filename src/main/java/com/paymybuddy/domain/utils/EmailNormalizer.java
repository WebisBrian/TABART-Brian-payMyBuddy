package com.paymybuddy.domain.utils;

public class EmailNormalizer {

    /**
     * Normalizes an email address by removing leading
     * and trailing spaces and converting it to lower case.*/
    public static String normalize(String email) {

        return email.trim().toLowerCase();
    }
}

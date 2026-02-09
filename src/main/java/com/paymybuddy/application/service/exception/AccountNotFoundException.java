package com.paymybuddy.application.service.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long userId) {
        super("Account not found for user ID: " + userId);
    }

    public AccountNotFoundException(String email) {
        super("Account not found for user email: " + email);
    }
}

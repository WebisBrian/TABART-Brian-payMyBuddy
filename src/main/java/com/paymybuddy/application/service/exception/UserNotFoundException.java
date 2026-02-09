package com.paymybuddy.application.service.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("Account not found for email: " + email);
    }
}

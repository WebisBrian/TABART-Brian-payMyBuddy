package com.paymybuddy.application.service.exception;

public class UserAccountNotFoundException extends RuntimeException {

    public UserAccountNotFoundException(Long userId) {
        super("Account not found for user ID: " + userId);
    }
}

package com.paymybuddy.application.service.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String email) {
        super("Profile not found for email: " + email);
    }
}

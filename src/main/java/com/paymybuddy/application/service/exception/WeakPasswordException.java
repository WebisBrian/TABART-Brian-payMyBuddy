package com.paymybuddy.application.service.exception;

public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException() {
        super("Password must contain at least 8 characters.");
    }
}

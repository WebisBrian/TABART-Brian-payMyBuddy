package com.paymybuddy.application.service.exception;

public class TooLongPasswordException extends RuntimeException {
    public TooLongPasswordException() {
        super("Password must not exceed 70 characters.");
    }
}

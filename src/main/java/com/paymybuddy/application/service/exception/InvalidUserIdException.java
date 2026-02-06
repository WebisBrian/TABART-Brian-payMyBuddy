package com.paymybuddy.application.service.exception;

public class InvalidUserIdException extends RuntimeException {

    public InvalidUserIdException() {
        super("User ID must not be null.");
    }
}

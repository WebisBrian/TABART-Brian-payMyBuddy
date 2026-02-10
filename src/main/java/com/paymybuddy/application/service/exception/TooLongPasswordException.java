package com.paymybuddy.application.service.exception;

public class TooLongPasswordException extends RuntimeException {
    public TooLongPasswordException(String message) {
        super(message);
    }
}

package com.paymybuddy.application.service.exception;

public class ContactAlreadyExistsException extends RuntimeException {
    public ContactAlreadyExistsException() {
        super("Contact already exists.");
    }
}

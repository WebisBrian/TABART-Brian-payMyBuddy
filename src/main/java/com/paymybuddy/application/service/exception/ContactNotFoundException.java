package com.paymybuddy.application.service.exception;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException() {
        super("Contact does not exist in your contacts list.");
    }
}

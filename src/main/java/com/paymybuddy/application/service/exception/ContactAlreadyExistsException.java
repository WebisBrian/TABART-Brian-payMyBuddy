package com.paymybuddy.application.service.exception;

public class ContactAlreadyExistsException extends RuntimeException {
    public ContactAlreadyExistsException(Long userID, Long contactId) {
        super("Contact already exists in the user's contacts list. Provided userId: " + userID + " / Provided contactId: " + contactId);
    }

    public ContactAlreadyExistsException(Long userID, String contactEmail) {
        super("Contact already exists in the user's contacts list. Provided userId: " + userID + " / Provided contact email: " + contactEmail);
    }
}

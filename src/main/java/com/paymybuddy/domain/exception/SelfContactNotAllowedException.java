package com.paymybuddy.domain.exception;

public class SelfContactNotAllowedException extends RuntimeException {

    public SelfContactNotAllowedException(Long userId) {
        super("User cannot add himself as a contact. Provided userId: " + userId + ".");
    }
}

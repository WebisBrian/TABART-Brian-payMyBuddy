package com.paymybuddy.domain.exception;

public class MissingAccountOwnerException extends RuntimeException {

    public MissingAccountOwnerException() {
        super("Account must be associated with a user.");
    }
}

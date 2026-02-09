package com.paymybuddy.application.service.exception;

public class NotInContactsException extends RuntimeException {

    public NotInContactsException() {
        super("Users must be contacts to transfer money");
    }
}

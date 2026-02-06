package com.paymybuddy.application.service.exception;

public class InvalidProfileUpdateParameterException extends RuntimeException {

    public InvalidProfileUpdateParameterException(String label) {
        super(label + " must not be blank or null.");
    }
}

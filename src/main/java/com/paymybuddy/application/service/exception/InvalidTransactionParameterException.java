package com.paymybuddy.application.service.exception;

public class InvalidTransactionParameterException extends RuntimeException {
    public InvalidTransactionParameterException(String label) {

        super(label + " must not be null.");
    }
}

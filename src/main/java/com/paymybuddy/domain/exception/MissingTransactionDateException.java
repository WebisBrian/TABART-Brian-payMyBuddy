package com.paymybuddy.domain.exception;

public class MissingTransactionDateException extends RuntimeException {

    public MissingTransactionDateException() {
        super("Transaction date is required.");
    }
}

package com.paymybuddy.domain.exception;

public class MissingReceiverAccountException extends RuntimeException {

    public MissingReceiverAccountException() {
        super("Receiver account is required.");
    }
}

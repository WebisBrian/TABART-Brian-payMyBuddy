package com.paymybuddy.domain.exception;

public class MissingSenderAccountException extends RuntimeException {

    public MissingSenderAccountException() {
        super("Sender account is required");
    }
}

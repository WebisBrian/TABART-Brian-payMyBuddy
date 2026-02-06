package com.paymybuddy.domain.exception;

public class InvalidUserFieldException extends RuntimeException {

    public InvalidUserFieldException(String field) {
        super(field + " must not be null or blank.");
    }
}

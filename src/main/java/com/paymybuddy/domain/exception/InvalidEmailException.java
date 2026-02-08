package com.paymybuddy.domain.exception;

public class InvalidEmailException extends RuntimeException
{
    public InvalidEmailException() {
        super("Email cannot be null or blank.");
    }
}

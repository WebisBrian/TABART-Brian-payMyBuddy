package com.paymybuddy.domain.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(BigDecimal amount) {
        super("Amount must be strictly positive. Provided: " + amount);
    }
}

package com.paymybuddy.domain.exception;

import java.math.BigDecimal;

public class InvalidTransactionAmountException extends RuntimeException {

    public InvalidTransactionAmountException(BigDecimal amount) {
        super("Amount must be strictly positive. Provided: " + amount);
    }
}

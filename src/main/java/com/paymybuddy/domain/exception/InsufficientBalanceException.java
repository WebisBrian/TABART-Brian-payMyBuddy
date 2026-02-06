package com.paymybuddy.domain.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(BigDecimal balance, BigDecimal amount) {
        super("Insufficient balance. Balance: " + balance + ", Amount: " + amount);
    }
}

package com.paymybuddy.domain.exception;

import java.math.BigDecimal;

public class InvalidTransactionFeeException extends RuntimeException {

    public InvalidTransactionFeeException(BigDecimal fee) {
        super("Fee must be zero or positive. Provided: " + fee);
    }
}

package com.paymybuddy.domain.exception;

import java.math.BigDecimal;

public class InvalidAmountParamException extends RuntimeException {

    public InvalidAmountParamException(BigDecimal amount) {
        super("Amount must be strictly positive. Provided: " + amount);
    }
}

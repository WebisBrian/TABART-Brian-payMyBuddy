package com.paymybuddy.domain.exception;

import java.math.BigDecimal;

public class InvalidMoneyAmountException extends RuntimeException {

    public InvalidMoneyAmountException(BigDecimal amount) {
        super("Amount must be strictly positive. Provided: " + amount);
    }
}

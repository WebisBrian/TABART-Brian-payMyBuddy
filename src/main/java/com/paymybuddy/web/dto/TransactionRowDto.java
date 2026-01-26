package com.paymybuddy.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
* DTO for a row in the transaction list. */
public record TransactionRowDto(
        LocalDateTime date,
        String counterpartyLabel,
        Direction direction,
        BigDecimal amount,
        BigDecimal fee,
        String description
) {

    public enum Direction {
        SENT, RECEIVED
    }
}

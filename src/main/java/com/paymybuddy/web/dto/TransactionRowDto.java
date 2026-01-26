package com.paymybuddy.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
* DTO for a row in the transaction list. */
public record TransactionRowDto(
        LocalDateTime date,
        // ex: "Alice (alice@email.com)
        String counterpartyLabel,
        Direction direction,
        // ex: "Sent" or "Received"
        String directionLabel,
        // -amount if SENT or +amount if RECEIVED
        BigDecimal signedAmount,
        BigDecimal fee,
        String description
) {

    public enum Direction {
        SENT, RECEIVED
    }
}

package com.paymybuddy.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionViewDto(
        LocalDateTime date,
        BigDecimal amount,
        BigDecimal fee,
        String description
) {
}

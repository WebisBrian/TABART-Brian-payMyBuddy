package com.paymybuddy.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TransferFormDto {

    @NotNull(message = "Receiver is required.")
    private Long receiverId;

    @NotNull(message = "Amount is required.")
    @Positive(message = "Amount should be positive.")
    private BigDecimal amount;

    @Size(max = 255, message = "Description should not exceed 255 characters.")
    private String description;

    public TransferFormDto() {
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

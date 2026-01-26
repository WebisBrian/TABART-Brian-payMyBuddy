package com.paymybuddy.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TransferFormDto {

    @NotNull(message = "Le destinataire est obligatoire")
    private Long receiverId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal amount;

    @Size(max = 255, message = "La description ne doit pas dépasser 255 caractères.")
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

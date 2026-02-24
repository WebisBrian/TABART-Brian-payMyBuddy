package com.paymybuddy.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddContactFormDto {

    @NotNull(message = "L' adresse email du contact est requise.")
    @NotBlank(message = "L' adresse email du contact est requise.")
    @Email(message = "L' adresse email doit être valide.")
    @Size(max = 255, message = "L' adresse email ne peut pas dépasser 255 caractères.")
    private String email;
}

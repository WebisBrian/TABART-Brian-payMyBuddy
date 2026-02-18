package com.paymybuddy.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileFormDto {

    @NotNull(message = "Le nom d'utilisateur est requis.")
    @NotBlank(message = "Le nom d'utilisateur ne peut pas être vide.")
    @Size(max = 100)
    private String newUsername;

    @NotNull(message = "L' adresse email est requise.")
    @NotBlank(message = "L' adresse email ne peut pas être vide.")
    @Email(message = "L' adresse email doit être valide.")
    @Size(max = 255, message = "L' adresse email ne peut pas dépasser 255 caractères.")
    private String newEmail;
}

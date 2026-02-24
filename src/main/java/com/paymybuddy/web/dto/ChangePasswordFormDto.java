package com.paymybuddy.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordFormDto {

    @NotNull(message = "Le mot de passe actuel est requis.")
    @NotBlank(message = "Le mot de passe actuel ne peut pas être vide.")
    private String currentPassword;

    @NotNull(message = "Le nouveau mot de passe est requis.")
    @NotBlank(message = "Le nouveau mot de passe ne peut pas être vide.")
    @Size(min = 8, max = 70, message = "Le mot de passe doit comporter entre 8 et 70 caractères.")
    private String newPassword;

    @NotNull(message = "La confirmation du mot de passe est requise.")
    @NotBlank(message = "La confirmation du mot de passe ne peut pas être vide.")
    private String confirmNewPassword;
}

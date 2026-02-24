package com.paymybuddy.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterFormDto {

    @NotNull(message = "Le nom d'utilisateur est requis.")
    @NotBlank(message = "Le nom d'utilisateur ne peut pas être vide.")
    @Size(max = 100)
    private String username;

    @NotNull(message = "L' adresse email est requise.")
    @NotBlank(message = "L' adresse email ne peut pas être vide.")
    @Email(message = "L' adresse email doit être valide.")
    @Size(max = 255, message = "L' adresse email ne peut pas dépasser 255 caractères.")
    private String email;

    @NotNull(message = "Le mot de passe est requis.")
    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    @Size(min = 8, max = 70, message = "Le mot de passe doit comporter entre 8 et 70 caractères.")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

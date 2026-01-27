package com.paymybuddy.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddContactFormDto {

    @NotBlank(message = "Contact's email is required.")
    @Email(message = "Contact's email must be a valid email address.")
    @Size(max = 255, message = "Contact's email must not exceed 255 characters.")
    private String email;
}

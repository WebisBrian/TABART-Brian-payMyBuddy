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
public class ProfileFormDto {

    @NotBlank
    @Size(max = 100)
    private String userName;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
}

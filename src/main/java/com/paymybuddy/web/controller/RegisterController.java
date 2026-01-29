package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.RegistrationService;
import com.paymybuddy.web.dto.RegisterFormDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    private final RegistrationService registrationService;

    public RegisterController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    String register() {
        return "register";
    }

    @PostMapping("/register")
    String postRegister(@Valid @ModelAttribute("registerForm") RegisterFormDto form,
                        BindingResult bindingResult) {

        registrationService.register(form.getUserName(), form.getEmail(), form.getPassword());

        return "redirect:/login";
    }
}

package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.RegistrationService;
import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.web.dto.RegisterFormDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    private final RegistrationService registrationService;

    public RegisterController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerForm", new RegisterFormDto());
        return "register";
    }

    @PostMapping("/register")
    String postRegister(@Valid @ModelAttribute("registerForm") RegisterFormDto form,
                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            registrationService.register(form.getUsername(), form.getEmail(), form.getPassword());

        } catch (EmailAlreadyUsedException e) {
            bindingResult.rejectValue("email", "emailAlreadyUsed", "Registration failed");
            return "register";
        }

        return "redirect:/login";
    }
}

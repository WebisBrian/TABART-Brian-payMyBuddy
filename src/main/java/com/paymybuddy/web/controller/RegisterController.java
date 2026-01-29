package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
    String postRegister(@RequestParam("userName") String userName,
                        @RequestParam("email") String email,
                        @RequestParam("password") String password) {

        registrationService.register(userName, email, password);

        return "redirect:/login";
    }
}

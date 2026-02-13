package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.RegistrationService;
import com.paymybuddy.web.dto.RegisterFormDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.paymybuddy.common.logging.SensitiveDataMasker.maskEmail;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final RegistrationService registrationService;

    public RegisterController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public String register(Model model) {
        logger.debug("GET /register called");
        model.addAttribute("registerForm", new RegisterFormDto());
        return "auth/register";
    }

    @PostMapping
    String postRegister(@Valid @ModelAttribute("registerForm") RegisterFormDto form,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {
        logger.info("POST /register called: usernamePresent={}, email={}",
                form.getUsername() != null && !form.getUsername().isBlank(),
                maskEmail(form.getEmail()));

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        registrationService.register(form.getUsername(), form.getEmail(), form.getPassword());
        redirectAttributes.addFlashAttribute("success", "Votre inscription est un succès. Vous pouvez maintenant vous connecter.");

        return "redirect:/login";
    }
}

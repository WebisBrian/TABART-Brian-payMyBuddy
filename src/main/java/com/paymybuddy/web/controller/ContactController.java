package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.UserService;
import com.paymybuddy.web.dto.AddContactFormDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/contacts")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final UserService userService;

    public ContactController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public String getContacts(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        String email = userDetails.getUsername();
        logger.debug("GET /contacts called: userEmail={}", maskEmail(email));

        Long userId = userService.getByEmail(email).getId();

        model.addAttribute("addContactForm", new AddContactFormDto());
        // TODO: optimize exposing only few details about contacts instead User entity -> DTO
        model.addAttribute("contacts", userService.listContacts(userId));

        return "contacts";
    }

    @PostMapping("/add")
    public String addContact(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute("addContactForm") AddContactFormDto form,
                             BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {
        String email = userDetails.getUsername();

        Long userId = userService.getByEmail(email).getId();
        logger.debug("POST /contacts/add called: userId={}, contactEmail={}",
                userId, maskEmail(form.getEmail()));

        if (bindingResult.hasErrors()) {
            model.addAttribute("contacts", userService.listContacts(userId));

            return "contacts";
        }

        userService.addContactByEmail(userId, form.getEmail());
        redirectAttributes.addFlashAttribute("addContactSuccess", "Contact ajouté avec succès.");

        return "redirect:/contacts";
    }
}

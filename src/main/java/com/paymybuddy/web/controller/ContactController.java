package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.UserService;
import com.paymybuddy.web.dto.AddContactFormDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    private final UserService userService;

    public ContactController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/contacts")
    public String getContacts(@RequestParam("userId") Long userId,
                              Model model) {

        model.addAttribute("addContactForm", new AddContactFormDto());
        model.addAttribute("contacts", userService.listContacts(userId));

        return "contacts";
    }

    @PostMapping("/contacts/add")
    public String addContact(@RequestParam("userId") Long userId,
                             @Valid @ModelAttribute("addContactForm") AddContactFormDto form,
                             BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("contacts", userService.listContacts(userId));

            return "contacts";
        }

        try {
            userService.addContactByEmail(userId, form.getEmail());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("addContactError", ex.getMessage());
            model.addAttribute("contacts", userService.listContacts(userId));

            return "contacts";
        }

        redirectAttributes.addFlashAttribute("addContactSuccess", "Contact added successfully.");

        return "redirect:/contacts?userId=" + userId;
    }
}

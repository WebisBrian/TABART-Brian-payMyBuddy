package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.ProfileService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.web.dto.ProfileFormDto;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserService userService;
    private final ProfileService profileService;

    public ProfileController(UserService userService, ProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String getProfile(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        String email = userDetails.getUsername();
        User user = userService.getByEmail(email);

        model.addAttribute("profileForm",
                new ProfileFormDto(user.getUsername(), user.getEmail()));

        return "profile";
    }

    @PostMapping("/profile/update")
    public String postUpdateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                    @Valid @ModelAttribute("profileForm") ProfileFormDto form,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
        return "profile";
    }

    profileService.updateProfile(userDetails.getUsername(), form.getNewUsername(), form.getNewEmail());

    redirectAttributes.addFlashAttribute("profileUpdateSuccess", "Profil mis à jour avec succès.");

    return "redirect:/profile";
    }
}

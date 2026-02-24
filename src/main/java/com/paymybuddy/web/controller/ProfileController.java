package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.ProfileService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.web.dto.ChangePasswordFormDto;
import com.paymybuddy.web.dto.ProfileFormDto;
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
@RequestMapping("/profile")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final UserService userService;
    private final ProfileService profileService;

    public ProfileController(UserService userService, ProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping
    public String getProfile(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        String email = userDetails.getUsername();
        logger.debug("GET /profile called: userEmail={}", maskEmail(email));

        User user = userService.getByEmail(email);

        model.addAttribute("profileForm",
                new ProfileFormDto(user.getUsername(), user.getEmail()));
        model.addAttribute("changePasswordForm", new ChangePasswordFormDto());
        model.addAttribute("activePage", "profile");

        return "app/profile";
    }

    @PostMapping("/update")
    public String postUpdateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                    @Valid @ModelAttribute("profileForm") ProfileFormDto form,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        String currentEmail = userDetails.getUsername();
        logger.info("POST /profile/update called: userEmail={}, newEmail={}",
                maskEmail(currentEmail), maskEmail(form.getNewEmail()));

        if (bindingResult.hasErrors()) {
            model.addAttribute("changePasswordForm", new ChangePasswordFormDto());
            model.addAttribute("activePage", "profile");
            return "app/profile";
        }

        profileService.updateProfile(userDetails.getUsername(), form.getNewUsername(), form.getNewEmail());

        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        return "redirect:/profile";
    }
    @PostMapping("/password")
    public String postChangePassword(@AuthenticationPrincipal UserDetails userDetails,
                                     @Valid @ModelAttribute("changePasswordForm") ChangePasswordFormDto form,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        logger.info("POST /profile/password called: userEmail={}", maskEmail(email));

        if (bindingResult.hasErrors()) {
            repopulateProfileForm(email, model);
            model.addAttribute("activePage", "profile");
            return "app/profile";
        }

        if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
            repopulateProfileForm(email, model);
            model.addAttribute("activePage", "profile");
            bindingResult.rejectValue("confirmNewPassword", "password.mismatch",
                    "La confirmation ne correspond pas au nouveau mot de passe.");
            return "app/profile";
        }

        profileService.changePassword(email, form.getCurrentPassword(), form.getNewPassword());
        redirectAttributes.addFlashAttribute("success", "Mot de passe mis à jour avec succès.");
        return "redirect:/profile";
    }

    /* Helpers */
    private void repopulateProfileForm(String email, Model model) {
        User user = userService.getByEmail(email);
        model.addAttribute("profileForm", new ProfileFormDto(user.getUsername(), user.getEmail()));
    }

}

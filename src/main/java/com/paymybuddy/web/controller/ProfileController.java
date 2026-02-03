package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.ProfileService;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
}

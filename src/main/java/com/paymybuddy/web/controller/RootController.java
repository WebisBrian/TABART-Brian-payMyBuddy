package com.paymybuddy.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    /**
     * Handle the root path to avoid Spring attempting to serve it as a static
     * resource (which caused NoResourceFoundException).
     * - If the user is authenticated, go to /transactions
     * - Otherwise redirect to the login page
     */
    @GetMapping("/")
    public String root(@AuthenticationPrincipal UserDetails user) {
        if (user != null) {
            return "redirect:/transactions";
        }
        return "redirect:/login";
    }
}


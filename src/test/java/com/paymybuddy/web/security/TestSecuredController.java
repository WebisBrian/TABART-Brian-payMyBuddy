package com.paymybuddy.web.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecuredController {

    @GetMapping("/secured-test")
    String securedTest() {
        return "Secured";
    }
}

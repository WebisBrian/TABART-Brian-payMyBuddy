package com.paymybuddy.web.controller;

import com.paymybuddy.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@Import(SecurityConfig.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getLogin_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void loginSuccess_shouldRedirectToTransactions() throws Exception {
        String email = "user@email.com";
        String password = "Password123!";
        String hash = passwordEncoder.encode(password);

        when(userDetailsService.loadUserByUsername(email)).thenReturn(
                User.withUsername(email)
                        .password(hash)
                        .roles("USER")
                        .build()
        );

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", email)
                        .param("password", password)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));
    }

    @Test
    void postLogin_shouldRedirectToLoginError_whenBadCredentials() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "bad@email.com")
                        .param("password", "wrong")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
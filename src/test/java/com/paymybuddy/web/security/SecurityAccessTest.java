package com.paymybuddy.web.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void accessProtectedPage_shouldRedirectToLogin_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void accessProtectedPage_shouldBeOk_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk());
    }
}

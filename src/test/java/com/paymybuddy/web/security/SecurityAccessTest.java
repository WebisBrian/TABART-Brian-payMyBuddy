package com.paymybuddy.web.security;

import com.paymybuddy.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TestSecuredController.class)
@Import(SecurityConfig.class)
public class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void accessProtectedPage_shouldRedirectToLogin_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/secured-test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    void accessProtectedPage_shouldBeOk_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/secured-test"))
                .andExpect(status().isOk());
    }
}

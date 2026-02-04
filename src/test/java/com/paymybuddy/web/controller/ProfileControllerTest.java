package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.ProfileService;
import com.paymybuddy.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    @WithMockUser(username = "user@email.com")
    void getProfile_shouldReturnProfileView() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postUpdateProfile_shouldBeForbidden_whenCsrfMissing() throws Exception {
        mockMvc.perform(post("/profile/update")
                        .param("newUserName", "newUser")
                        .param("newEmail", "new@email.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(profileService);
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postUpdateProfile_shouldRedirectAndCallService_whenCsrfPresent() throws Exception {
        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("newUserName", "newUser")
                        .param("newEmail", "new@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(profileService).updateProfile("user@email.com", "newUser", "new@email.com");
    }

}
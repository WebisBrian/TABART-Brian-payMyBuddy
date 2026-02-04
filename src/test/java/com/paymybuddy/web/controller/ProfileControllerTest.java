package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.ProfileService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProfileService profileService;

    @Test
    @WithMockUser(username = "user@email.com")
    void getProfilePage_shouldRenderViewAndPreparePrefilledForm() throws Exception {
        when(userService.getByEmail("user@email.com"))
                .thenReturn(userWithId(1L, "existingName", "user@email.com"));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("profileForm"))
                // verify that the form is pre-filled
                .andExpect(model().attribute("profileForm", allOf(
                        hasProperty("newUsername", is("existingName")),
                        hasProperty("newEmail", is("user@email.com"))
                )));

        verify(userService).getByEmail("user@email.com");
        verifyNoInteractions(profileService);
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postUpdateProfile_shouldRedirect_whenValid() throws Exception {
        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("newUsername", "newUser")
                        .param("newEmail", "new@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(profileService).updateProfile(
                eq("user@email.com"),
                eq("newUser"),
                eq("new@email.com")
        );
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postUpdateProfile_shouldReturnView_whenValidationFails() throws Exception {
        // newUsername missing
        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("newEmail", "new@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("profileForm", "newUsername"));

        verifyNoInteractions(profileService);
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postUpdateProfile_shouldReturnView_withErrorMessage_whenServiceThrows() throws Exception {
        doThrow(new IllegalArgumentException("Email already used."))
                .when(profileService)
                .updateProfile("user@email.com", "newUser", "taken@email.com");

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("newUsername", "newUser")
                        .param("newEmail", "taken@email.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attribute("profileError", "Email already used."));

        verify(profileService).updateProfile("user@email.com", "newUser", "taken@email.com");
    }

    private User userWithId(long id, String username, String email) {
        User user = User.create(username, email, "password");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
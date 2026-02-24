package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.ProfileService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.application.service.exception.InvalidCurrentPasswordException;
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

    /* ---------- getProfilePage() ---------- */
    @Test
    @WithMockUser(username = "user@email.com")
    void getProfilePage_shouldRenderViewAndPreparePrefilledForm() throws Exception {
        when(userService.getByEmail("user@email.com"))
                .thenReturn(userWithId(1L, "existingName", "user@email.com"));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/profile"))
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
    void getProfilePage_shouldPrepareChangePasswordForm() throws Exception {
        when(userService.getByEmail("user@email.com"))
                .thenReturn(userWithId(1L, "existingName", "user@email.com"));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/profile"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("changePasswordForm"));

        verify(userService).getByEmail("user@email.com");
        verifyNoInteractions(profileService);
    }

    /* ---------- postUpdateProfile() ---------- */
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
                .andExpect(view().name("app/profile"))
                .andExpect(model().attributeHasFieldErrors("profileForm", "newUsername"));

        verifyNoInteractions(profileService);
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postUpdateProfile_shouldRedirectWithErrorMessage_whenServiceThrows() throws Exception {
        doThrow(new EmailAlreadyUsedException("new@email.com"))
                .when(profileService)
                .updateProfile("user@email.com", "newUser", "new@email.com");

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("newUsername", "newUser")
                        .param("newEmail", "new@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Cette adresse email est déjà utilisée."));

        verify(profileService).updateProfile("user@email.com", "newUser", "new@email.com");
    }

    /* ---------- postChangePassword() ---------- */
    @Test
    @WithMockUser(username = "user@email.com")
    void postChangePassword_shouldRedirect_whenValid() throws Exception {
        mockMvc.perform(post("/profile/password")
                        .with(csrf())
                        .param("currentPassword", "CurrentPwd123!")
                        .param("newPassword", "NewPwd123!")
                        .param("confirmNewPassword", "NewPwd123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("success", "Mot de passe mis à jour avec succès."));

        verify(profileService).changePassword("user@email.com", "CurrentPwd123!", "NewPwd123!");
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postChangePassword_shouldReturnView_whenValidationFails() throws Exception {
        when(userService.getByEmail("user@email.com"))
                .thenReturn(userWithId(1L, "existingName", "user@email.com"));

        mockMvc.perform(post("/profile/password")
                        .with(csrf())
                        // currentPassword missing
                        .param("newPassword", "NewPwd123!")
                        .param("confirmNewPassword", "NewPwd123!"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/profile"))
                .andExpect(model().attributeHasFieldErrors("changePasswordForm", "currentPassword"))
                .andExpect(model().attributeExists("profileForm"))
                .andExpect(model().attributeExists("activePage"));

        verifyNoInteractions(profileService);
        verify(userService).getByEmail("user@email.com");
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postChangePassword_shouldReturnView_whenConfirmDoesNotMatch() throws Exception {
        when(userService.getByEmail("user@email.com"))
                .thenReturn(userWithId(1L, "existingName", "user@email.com"));

        mockMvc.perform(post("/profile/password")
                        .with(csrf())
                        .param("currentPassword", "CurrentPwd123!")
                        .param("newPassword", "NewPwd123!")
                        .param("confirmNewPassword", "DifferentPwd123!"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/profile"))
                .andExpect(model().attributeHasFieldErrors("changePasswordForm", "confirmNewPassword"));

        verifyNoInteractions(profileService);
        verify(userService).getByEmail("user@email.com");
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postChangePassword_shouldRedirectWithErrorMessage_whenServiceThrowsInvalidCurrentPassword() throws Exception {
        doThrow(new InvalidCurrentPasswordException())
                .when(profileService).changePassword("user@email.com", "bad", "NewPwd123!");

        mockMvc.perform(post("/profile/password")
                        .with(csrf())
                        .param("currentPassword", "bad")
                        .param("newPassword", "NewPwd123!")
                        .param("confirmNewPassword", "NewPwd123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Mot de passe actuel incorrect."));

        verify(profileService).changePassword("user@email.com", "bad", "NewPwd123!");
    }

    /* ---------- Helpers ---------- */
    private User userWithId(long id, String username, String email) {
        User user = User.create(username, email, "password");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
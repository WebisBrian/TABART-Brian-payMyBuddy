package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.RegistrationService;
import com.paymybuddy.application.service.exception.EmailAlreadyUsedException;
import com.paymybuddy.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegisterController.class)
@Import(SecurityConfig.class)
class RegisterControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @Test
    void getRegister_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void postRegister_shouldRedirectToLogin_whenValid_andCsrfPresent() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("userName", "Brian")
                        .param("email", "brian@email.com")
                        .param("password", "Password123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(registrationService).register("Brian", "brian@email.com", "Password123!");
    }

    @Test
    void postRegister_shouldReturn403_whenCsrfMissing() throws Exception {
        mockMvc.perform(post("/register")
                .param("userName", "Brian")
                .param("email", "brian@email.com")
                .param("password", "Password123!")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void postRegister_shouldReturnRegisterView_whenValidationFails() throws Exception {
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("userName", "")
                .param("email", "not-an-email")
                .param("password", "123")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "userName", "email", "password"));

        verify(registrationService, never()).register(anyString(), anyString(), anyString());
    }

    @Test
    void postRegister_shouldReturnRegisterView_whenEmailAlreadyExists() throws Exception {
        doThrow(new EmailAlreadyUsedException("brian@email.com"))
                .when(registrationService)
                .register("Brian", "brian@email.com", "Password123!");

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("userName", "Brian")
                .param("email", "brian@email.com")
                .param("password", "Password123!")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "email"));

        verify(registrationService).register("Brian", "brian@email.com", "Password123!");
    }
}
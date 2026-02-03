package com.paymybuddy.web.controller;

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

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "user@email.com")
    void getContactsPage_shouldRenderViewAndPrepareModel() throws Exception {
        when(userService.getByEmail("user@email.com")).thenReturn(userWithId(1L, "user@email.com"));
        when(userService.listContacts(eq(1L)))
                .thenReturn(List.of());

        // Act + Assert
        mockMvc.perform(get("/contacts")
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("addContactForm"))
                .andExpect(model().attributeExists("contacts"));

        verify(userService).getByEmail("user@email.com");
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postAddContact_shouldRedirect_whenValid() throws Exception {
        when(userService.getByEmail("user@email.com")).thenReturn(userWithId(1L, "user@email.com"));

        doNothing().when(userService).addContactByEmail(
                eq(1L),
                eq("contact@email.com")
        );

        // Act + Assert
        mockMvc.perform(post("/contacts/add")
                        .with(csrf())
                        .param("email", "contact@email.com")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"));

        verify(userService).getByEmail("user@email.com");
        verify(userService).addContactByEmail(1L, "contact@email.com");
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postAddContact_shouldReturnView_whenValidationFails() throws Exception {
        when(userService.getByEmail("user@email.com")).thenReturn(userWithId(1L, "user@email.com"));
        when(userService.listContacts(eq(1L)))
                .thenReturn(List.of());

        // Act + Assert
        mockMvc.perform(post("/contacts/add")
                                .with(csrf())
                        // the email is missing
                )
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeHasFieldErrors("addContactForm", "email"))
                .andExpect(model().attributeExists("contacts"));

        verify(userService).getByEmail("user@email.com");
        verify(userService, never()).addContactByEmail(anyLong(), anyString());
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void postAddContact_shouldReturnView_withErrorMessage_whenServiceThrows() throws Exception {
        when(userService.getByEmail("user@email.com")).thenReturn(userWithId(1L, "user@email.com"));
        when(userService.listContacts(eq(1L)))
                .thenReturn(List.of());

        doThrow(new IllegalArgumentException("Cannot add self as contact."))
                .when(userService)
                .addContactByEmail(
                        eq(1L),
                        eq("user@email.com")
                );

        // Act + Assert
        mockMvc.perform(post("/contacts/add")
                        .with(csrf())
                        .param("email", "user@email.com")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("addContactForm"))
                .andExpect(model().attributeExists("contacts"))
                .andExpect(model().attribute("addContactError", "Cannot add self as contact."));

        verify(userService).getByEmail("user@email.com");
    }

    private User userWithId(long id, String email) {
        User user = User.create("user", email, "password");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void getContactsPage_shouldRenderViewAndPrepareModel() throws Exception {
        when(userService.listContacts(eq(1L)))
                .thenReturn(List.of());

        // Act + Assert
        mockMvc.perform(get("/contacts").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("addContactForm"))
                .andExpect(model().attributeExists("contacts"));
    }

    @Test
    void postAddContact_shouldRedirect_whenValid() throws Exception {
        doNothing().when(userService).addContactByEmail(
                eq(1L),
                eq("contact@email.com")
        );

        // Act + Assert
        mockMvc.perform(post("/contacts/add")
                .param("userId", "1")
                .param("email", "contact@email.com")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts?userId=1"));

        verify(userService).addContactByEmail(1L, "contact@email.com");
    }

    @Test
    void postAddContact_shouldReturnView_whenValidationFails() throws Exception {
        when(userService.listContacts(eq(1L)))
                .thenReturn(List.of());

        // Act + Assert
        mockMvc.perform(post("/contacts/add")
                                .param("userId", "1")
                        // the email is missing
                )
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeHasFieldErrors("addContactForm", "email"))
                .andExpect(model().attributeExists("contacts"));

        verify(userService, never()).addContactByEmail(anyLong(), anyString());
    }

    @Test
    void postAddContact_shouldReturnView_withErrorMessage_whenServiceThrows() throws Exception {
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
                .param("userId", "1")
                .param("email", "user@email.com")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("addContactForm"))
                .andExpect(model().attributeExists("contacts"))
                .andExpect(model().attribute("addContactError", "Cannot add self as contact."));
    }
}
package com.paymybuddy.web.exception;

import com.paymybuddy.application.service.ProfileService;
import com.paymybuddy.application.service.RegistrationService;
import com.paymybuddy.application.service.TransactionService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.application.service.exception.*;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.exception.*;
import com.paymybuddy.infrastructure.security.SecurityConfig;
import com.paymybuddy.web.controller.ContactController;
import com.paymybuddy.web.controller.ProfileController;
import com.paymybuddy.web.controller.RegisterController;
import com.paymybuddy.web.controller.TransactionController;
import com.paymybuddy.web.mapper.TransactionRowMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        TransactionController.class,
        ContactController.class,
        RegisterController.class,
        ProfileController.class})
@Import(SecurityConfig.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private TransactionRowMapper transactionRowMapper;

    /* ---------- Business specifics exceptions ---------- */
    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleAccountNotFoundException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new AccountNotFoundException(50L))
                .when(transactionService).transfer(anyLong(), anyLong(), any(BigDecimal.class), anyString());

        mockMvc.perform(post("/transactions/transfer")
                        .with(csrf())
                        .param("receiverId", "50")
                        .param("amount", "5.00")
                        .param("description", "Dinner"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Compte non trouvé pour la transaction."));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleContactAlreadyExistsException() throws Exception {
        Long userId = 1L;
        String contactEmail = "contact@email.com";
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new ContactAlreadyExistsException(userId,contactEmail))
                .when(userService).addContactByEmail(userId,contactEmail);

        mockMvc.perform(post("/contacts/add")
                        .with(csrf())
                        .param("email", "contact@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Contact déjà présent dans la liste de contacts."));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleSelfTransferException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new SelfTransferException(1L, 1L))
                .when(transactionService).transfer(anyLong(), anyLong(), any(BigDecimal.class), anyString());

        mockMvc.perform(post("/transactions/transfer")
                        .with(csrf())
                        .param("receiverId", "1")
                        .param("amount", "10.00")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Vous ne pouvez pas effectuer un transfert vers vous-même."));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleNotInContactsException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new NotInContactsException(1L, 99L))
                .when(transactionService).transfer(anyLong(), anyLong(), any(BigDecimal.class), anyString());

        mockMvc.perform(post("/transactions/transfer")
                        .with(csrf())
                        .param("receiverId", "99")
                        .param("amount", "10.00")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Le destinataire doit être dans votre liste de contacts."));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleEmailAlreadyUsedException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new EmailAlreadyUsedException("new@email.com"))
                .when(profileService).updateProfile(anyString(), anyString(), anyString());

        mockMvc.perform(post("/profile/update")
                        .with(csrf())
                        .param("newUsername", "newUsername")
                        .param("newEmail", "new@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Cette adresse email est déjà utilisée."));
    }

    @Test
    void shouldHandleWeakPasswordException() throws Exception {
        doThrow(new WeakPasswordException("weak"))
                .when(registrationService).register(anyString(), anyString(), anyString());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "user")
                        .param("email", "user@email.com")
                        .param("password", "password")) // normal password to avoid validation error from @Valid
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessageFromGEH",
                        "Le mot de passe doit comporter au moins 8 caractères, ne pas être vide ou composé uniquement d'espaces."));
    }

    @Test
    void shouldHandleTooLongPasswordException() throws Exception {
        doThrow(new TooLongPasswordException())
                .when(registrationService).register(anyString(), anyString(), anyString());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "user")
                        .param("email", "user@email.com")
                        .param("password", "password")) // normal password to avoid validation error from @Valid
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessageFromGEH",
                        "Le mot de passe ne peut pas dépasser 70 caractères."));
    }

    /* ---------- Domain exceptions ---------- */

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleInsufficientBalanceException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new InsufficientBalanceException(BigDecimal.valueOf(50), BigDecimal.valueOf(100)))
                .when(transactionService).transfer(anyLong(), anyLong(), any(BigDecimal.class), anyString());

        mockMvc.perform(post("/transactions/transfer")
                        .with(csrf())
                        .param("receiverId", "2")
                        .param("amount", "100.00")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Solde insuffisant."));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleInvalidAmountException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new InvalidAmountException("Amount must be positive"))
                .when(transactionService).transfer(anyLong(), anyLong(), any(BigDecimal.class), anyString());

        mockMvc.perform(post("/transactions/transfer")
                        .with(csrf())
                        .param("receiverId", "2")
                        .param("amount", "10.00")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Le montant doit être strictement positif."));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleInvalidEmailException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new InvalidEmailException("Invalid email format"))
                .when(userService).addContactByEmail(anyLong(), anyString());

        mockMvc.perform(post("/contacts/add")
                        .with(csrf())
                        .param("email", "bad@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Adresse email invalide."));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleSelfContactNotAllowedException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new SelfContactNotAllowedException(1L))
                .when(userService).addContactByEmail(anyLong(), anyString());

        mockMvc.perform(post("/contacts/add")
                        .with(csrf())
                        .param("email", "user@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Vous ne pouvez pas vous ajouter comme contact."));
    }

    /* ---------- Generic exceptions ---------- */
    @Test
    void shouldHandleIllegalArgumentExceptionFromRegistration() throws Exception {
        doThrow(new IllegalArgumentException("Requête invalide."))
                .when(registrationService).register(anyString(), anyString(), anyString());

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "user")
                        .param("email", "user@example.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessageFromGEH", "Requête invalide."));
    }

    /* ---------- others ---------- */

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleUserNotFoundException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new UserNotFoundException("unknown@email.com"))
                .when(userService).addContactByEmail(anyLong(), anyString());

        mockMvc.perform(post("/contacts/add")
                        .with(csrf())
                        .param("email", "unknown@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("error", "Utilisateur non trouvé."));
    }

    @Test
    @WithMockUser(username = "user@email.com")
    void shouldHandleContactNotFoundException() throws Exception {
        when(userService.getByEmail(anyString())).thenReturn(createMockUser());
        doThrow(new ContactNotFoundException())
                .when(userService).addContactByEmail(anyLong(), anyString());

        mockMvc.perform(post("/contacts/add")
                        .with(csrf())
                        .param("email", "contact@email.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("error", "Contact non trouvé."));
    }

    /* ---------- Helper methods ---------- */

    private User createMockUser() {
        User user = User.create("user", "user@email.com", "password");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}


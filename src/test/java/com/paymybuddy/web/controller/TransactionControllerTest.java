package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.TransactionService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.web.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TransactionMapper transactionMapper;

    @Test
    @WithMockUser
    void getTransactionsPage_shouldRenderViewAndPrepareModel() throws Exception {
        when(transactionService.getTransactionHistory(eq(1L), any()))
                .thenReturn(Page.empty());

        when(userService.listContacts(eq(1L)))
                .thenReturn(List.of());

        // Act + Assert
        mockMvc.perform(get("/transactions").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transferForm"))
                .andExpect(model().attributeExists("contacts"))
                .andExpect(model().attributeExists("transactionHistory"));
    }

    @Test
    void postTransfer_shouldRedirectToTransactionsPage_whenValid() throws Exception {
        when(userService.listContacts(eq(1L))).thenReturn(List.of());
        when(transactionService.getTransactionHistory(eq(1L), any())).thenReturn(Page.empty());

        doNothing().when(transactionService).transfer(
                eq(1L),
                eq(2L),
                eq(new BigDecimal("5.00")),
                eq("Dinner")
        );

        // Act + Assert
        mockMvc.perform(post("/transactions/transfer")
                .param("userId", "1")
                .param("receiverId", "2")
                .param("amount", "5.00")
                .param("description", "Dinner")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions?userId=1"));
    }

    @Test
    void postTransfer_shouldReturnTransactionsView_whenValidationFails() throws Exception {
        when(userService.listContacts(eq(1L))).thenReturn(List.of());
        when(transactionService.getTransactionHistory(eq(1L), any())).thenReturn(Page.empty());

        // Act + Assert
        mockMvc.perform(post("/transactions/transfer")
                .param("userId", "1")
                .param("receiverId", "2")
                // amount is missing
                .param("description", "Dinner")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeHasFieldErrors("transferForm", "amount"))
                .andExpect(model().attributeExists("contacts"))
                .andExpect(model().attributeExists("transactionHistory"));

        verify(transactionService, never()).transfer(any(), any(), any(), any());
    }

    @Test
    void postTransfer_shouldReturnTransactionsView_withErrorMessage_whenServiceThrows() throws Exception {
        when(userService.listContacts(eq(1L))).thenReturn(List.of());
        when(transactionService.getTransactionHistory(eq(1L), any())).thenReturn(Page.empty());

        doThrow(new IllegalArgumentException("Insufficient balance in sender account."))
                .when(transactionService)
                .transfer(eq(1L), eq(2L), eq(new BigDecimal("5.00")), eq("Dinner"));

        // Act + Assert
        mockMvc.perform(post("/transactions/transfer")
                .param("userId", "1")
                .param("receiverId", "2")
                .param("amount", "5.00")
                .param("description", "Dinner")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transferForm"))
                .andExpect(model().attributeExists("contacts"))
                .andExpect(model().attributeExists("transactionHistory"))
                .andExpect(model().attribute("transferError", "Insufficient balance in sender account."));
    }
}
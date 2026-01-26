package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.TransactionService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.web.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TransactionController.class)
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
}
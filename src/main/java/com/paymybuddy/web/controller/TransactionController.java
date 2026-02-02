package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.TransactionService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.web.dto.TransferFormDto;
import com.paymybuddy.web.dto.TransactionRowDto;
import com.paymybuddy.web.mapper.TransactionRowMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final TransactionRowMapper transactionRowMapper;

    public TransactionController(TransactionService transactionService,
                                 UserService userService,
                                 TransactionRowMapper transactionRowMapper) {
        this.transactionService = transactionService;
        this.userService = userService;
        this.transactionRowMapper = transactionRowMapper;
    }

    @GetMapping("/transactions")
    public String getTransactions(@AuthenticationPrincipal UserDetails userDetails,
                                  @PageableDefault(size = 10, sort = "date") Pageable pageable,
                                  Model model) {
        String email = userDetails.getUsername();
        Long userId = userService.getByEmail(email).getId();

        model.addAttribute("transferForm", new TransferFormDto());
        model.addAttribute("contacts", userService.listContacts(userId));

        Page<TransactionRowDto> rows = transactionService.getTransactionHistory(userId, pageable)
                .map(tx -> transactionRowMapper.toRowDto(tx, userId));

        model.addAttribute("transactionRows", rows);

        return "transactions";
    }

    @PostMapping("/transactions/transfer")
    public String transfer(@AuthenticationPrincipal UserDetails userDetails,
                           @Valid @ModelAttribute("transferForm") TransferFormDto form,
                           BindingResult bindingResult,
                           @PageableDefault(size = 10, sort = "date") Pageable pageable,
                           Model model) {
        String email = userDetails.getUsername();
        Long userId = userService.getByEmail(email).getId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("contacts", userService.listContacts(userId));

            Page<TransactionRowDto> rows = transactionService.getTransactionHistory(userId, pageable)
                    .map(tx -> transactionRowMapper.toRowDto(tx, userId));
            model.addAttribute("transactionRows", rows);

            return "transactions";
        }

        try {
            transactionService.transfer(userId, form.getReceiverId(), form.getAmount(), form.getDescription());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("transferError", ex.getMessage());
            model.addAttribute("contacts", userService.listContacts(userId));

            Page<TransactionRowDto> rows = transactionService.getTransactionHistory(userId, pageable)
                    .map(tx -> transactionRowMapper.toRowDto(tx, userId));
            model.addAttribute("transactionRows", rows);

            return "transactions";
        }

        return "redirect:/transactions";
    }
}

package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.TransactionService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.web.dto.TransferFormDto;
import com.paymybuddy.web.mapper.TransactionMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final UserService userService;


    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper, UserService userService) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
        this.userService = userService;
    }

    @GetMapping("/transactions")
    public String getTransactions(@RequestParam("userId") Long userId,
                                  @PageableDefault(size = 10, sort = "date") Pageable pageable,
                                  Model model) {
        model.addAttribute("transferForm", new TransferFormDto());
        model.addAttribute("contacts", userService.listContacts(userId));
        model.addAttribute("transactionHistory", transactionService.getTransactionHistory(userId, pageable));

        return "transactions";
    }

    @PostMapping("/transactions/transfer")
    public String transfer(@RequestParam("userId") Long userId,
                           @RequestParam("receiverId") Long receiverId,
                           @RequestParam("amount") BigDecimal amount,
                           @RequestParam(value = "description", required = false) String description) {

        transactionService.transfer(userId, receiverId, amount, description);

        return "redirect:/transactions?userId=" + userId;
    }
}

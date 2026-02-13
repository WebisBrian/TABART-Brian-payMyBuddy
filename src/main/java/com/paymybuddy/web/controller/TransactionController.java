package com.paymybuddy.web.controller;

import com.paymybuddy.application.service.TransactionService;
import com.paymybuddy.application.service.UserService;
import com.paymybuddy.web.dto.ContactViewDto;
import com.paymybuddy.web.dto.TransferFormDto;
import com.paymybuddy.web.dto.TransactionRowDto;
import com.paymybuddy.web.mapper.TransactionRowMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.paymybuddy.common.logging.SensitiveDataMasker.maskEmail;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

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

    @GetMapping
    public String getTransactions(@AuthenticationPrincipal UserDetails userDetails,
                                  @PageableDefault(size = 10, sort = "date") Pageable pageable,
                                  Model model) {
        String email = userDetails.getUsername();
        logger.debug("GET /transactions called: userEmail={}", maskEmail(email));

        Long userId = userService.getByEmail(email).getId();
        List<ContactViewDto> contacts = mapToContactViewDtos(userId);

        model.addAttribute("transferForm", new TransferFormDto());
        model.addAttribute("contacts", contacts);

        Page<TransactionRowDto> rows = transactionService.getTransactionHistory(userId, pageable)
                .map(tx -> transactionRowMapper.toRowDto(tx, userId));

        model.addAttribute("transactionRows", rows);

        return "transactions";
    }

    @PostMapping("/transfer")
    public String transfer(@AuthenticationPrincipal UserDetails userDetails,
                           @Valid @ModelAttribute("transferForm") TransferFormDto form,
                           BindingResult bindingResult,
                           @PageableDefault(size = 10, sort = "date") Pageable pageable,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String email = userDetails.getUsername();
        Long userId = userService.getByEmail(email).getId();

        logger.info("POST /transactions/transfer called: userId={}, receiverId={}, amount={}, description={}",
                userId, form.getReceiverId(), form.getAmount(), form.getDescription());

        if (bindingResult.hasErrors()) {
            List<ContactViewDto> contacts = mapToContactViewDtos(userId);
            model.addAttribute("contacts", contacts);

            Page<TransactionRowDto> rows = transactionService.getTransactionHistory(userId, pageable)
                    .map(tx -> transactionRowMapper.toRowDto(tx, userId));
            model.addAttribute("transactionRows", rows);

            return "transactions";
        }

        transactionService.transfer(userId, form.getReceiverId(), form.getAmount(), form.getDescription());
        redirectAttributes.addFlashAttribute("success", "Transfert effectué avec succès.");

        return "redirect:/transactions";
    }

    /* Helpers */
    private List<ContactViewDto> mapToContactViewDtos(Long userId) {
        return userService.listContacts(userId).stream()
                .map(user -> new ContactViewDto(user.getId(), user.getUsername(), user.getEmail()))
                .toList();
    }
}

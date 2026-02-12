package com.paymybuddy.web.exception;

import com.paymybuddy.application.service.exception.*;
import com.paymybuddy.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* ---------- Business specifics exceptions ---------- */
    @ExceptionHandler(AccountNotFoundException.class)
    public String handleAccountNotFoundException(AccountNotFoundException ex,
                                                 RedirectAttributes redirectAttributes) {
        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Compte non trouvé pour la transaction.");
        return "redirect:/transactions";
    }

    @ExceptionHandler(ContactAlreadyExistsException.class)
    public String handleContactAlreadyExistsException(ContactAlreadyExistsException ex,
                                                      RedirectAttributes redirectAttributes) {
        logger.info(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Contact déjà présent dans la liste de contacts.");

        return "redirect:/contacts";
    }

    @ExceptionHandler(SelfTransferException.class)
    public String handleSelfTransferException(SelfTransferException ex,
                                              RedirectAttributes redirectAttributes) {
        logger.info(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Vous ne pouvez pas effectuer un transfert vers vous-même.");
        return "redirect:/transactions";
    }

    @ExceptionHandler(NotInContactsException.class)
    public String handleNotInContactsException(NotInContactsException ex,
                                               RedirectAttributes redirectAttributes) {
        logger.warn("Not in contacts: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Le destinataire doit être dans votre liste de contacts.");
        return "redirect:/transactions";
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public String handleEmailAlreadyUsedException(EmailAlreadyUsedException ex,
                                                  HttpServletRequest request,
                                                  RedirectAttributes redirectAttributes) {
        String path = request.getRequestURI();

        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Cette adresse email est déjà utilisée.");

        if (path.startsWith("/register")) {
            return "redirect:/register";
        }

        if (path.startsWith("/profile")) {
            return "redirect:/profile";
        }

        return "redirect:/profile";
    }

    @ExceptionHandler(WeakPasswordException.class)
    public String handleWeakPasswordException(WeakPasswordException ex,
                                              RedirectAttributes redirectAttributes) {
        logger.warn("Weak password: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Le mot de passe doit comporter au moins 8 caractères, ne pas être vide ou composé uniquement d'espaces.");
        return "redirect:/register";
    }

    @ExceptionHandler(TooLongPasswordException.class)
    public String handleTooLongPasswordException(TooLongPasswordException ex,
                                                 RedirectAttributes redirectAttributes) {
        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Le mot de passe ne peut pas dépasser 70 caractères.");
        return "redirect:/register";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex,
                                              RedirectAttributes redirectAttributes,
                                              HttpServletResponse response) {
        logger.warn("User not found: {}", ex.getMessage());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé.");
        return "redirect:/contacts";
    }

    /* ---------- Domain specifics exceptions ---------- */

    @ExceptionHandler(InsufficientBalanceException.class)
    public String handleInsufficientBalanceException(InsufficientBalanceException ex,
                                                     RedirectAttributes redirectAttributes) {
        logger.info(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Solde insuffisant.");
        return "redirect:/transactions";
    }

    @ExceptionHandler(InvalidAmountException.class)
    public String handleInvalidAmountException(InvalidAmountException ex,
                                               RedirectAttributes redirectAttributes) {
        logger.info(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Le montant doit être strictement positif.");
        return "redirect:/transactions";
    }

    @ExceptionHandler(InvalidEmailException.class)
    public String handleInvalidEmailException(InvalidEmailException ex,
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        String path = request.getRequestURI();

        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Adresse email invalide.");

        if (path.startsWith("/contacts")) {
            return "redirect:/contacts";
        }
        if (path.startsWith("/login")) {
            return "redirect:/login";
        }
        if (path.startsWith("/register")) {
            return "redirect:/register";
        }

        return "redirect:/";
    }

    @ExceptionHandler(SelfContactNotAllowedException.class)
    public String handleSelfContactNotAllowedException(SelfContactNotAllowedException ex,
                                                       RedirectAttributes redirectAttributes) {
        logger.info(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Vous ne pouvez pas vous ajouter comme contact.");
        return "redirect:/contacts";
    }
    /* ---------- Generic exceptions ---------- */

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex,
                                                 RedirectAttributes redirectAttributes,
                                                 HttpServletRequest request
                                                 ) {
        String path = request.getRequestURI();

        logger.warn("Invalid argument. Detail = ({})", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Requête invalide.");

        if (path.startsWith("/transactions")) {
            return "redirect:/transactions";
        }

        if (path.startsWith("/contacts")) {
            return "redirect:/contacts";
        }

        if (path.startsWith("/profile")) {
            return "redirect:/profile";
        }

        if (path.startsWith("/register")) {
            return "redirect:/register";
        }

        if (path.startsWith("/login")) {
            return "redirect:/login";
        }

        return "redirect:/";
    }


    /* ---------- others ---------- */

    @ExceptionHandler(ProfileNotFoundException.class)
    public String handleProfileNotFoundException(ProfileNotFoundException ex,
                                                 RedirectAttributes redirectAttributes,
                                                 HttpServletResponse response) {
        logger.warn("Profile not found: {}", ex.getMessage());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        redirectAttributes.addFlashAttribute("error", "Profil non trouvé.");
        return "redirect:/profile";
    }

    @ExceptionHandler(ContactNotFoundException.class)
    public String handleContactNotFoundException(ContactNotFoundException ex,
                                                 RedirectAttributes redirectAttributes,
                                                 HttpServletResponse response) {
        logger.warn("Contact not found: {}", ex.getMessage());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        redirectAttributes.addFlashAttribute("error", "Contact non trouvé.");
        return "redirect:/contacts";
    }
}

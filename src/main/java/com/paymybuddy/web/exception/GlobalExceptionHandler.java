package com.paymybuddy.web.exception;

import com.paymybuddy.application.service.exception.*;
import com.paymybuddy.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Cette adresse email est déjà utilisée.");

        return determineRedirectUrl(request);
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
                                              RedirectAttributes redirectAttributes) {
        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Utilisateur non trouvé avec l'adresse email renseignée.");
        return "redirect:/contacts";
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public String handleProfileNotFoundException(ProfileNotFoundException ex,
                                                 RedirectAttributes redirectAttributes) {
        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Profil non trouvé avec l'adresse email renseignée.");
        return "redirect:/profile";
    }

    @ExceptionHandler(ContactNotFoundException.class)
    public String handleContactNotFoundException(ContactNotFoundException ex,
                                                 RedirectAttributes redirectAttributes) {
        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Contact non trouvé.");
        return "redirect:/contacts";
    }

    /* ---------- Domain specifics exceptions ---------- */
    @ExceptionHandler(InsufficientBalanceException.class)
    public String handleInsufficientBalanceException(InsufficientBalanceException ex,
                                                     RedirectAttributes redirectAttributes) {
        logger.info(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Solde insuffisant pour assurer la transaction et les frais d'envoi.");
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

        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Adresse email invalide.");

        return determineRedirectUrl(request);
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

        logger.warn("Invalid argument. Detail = ({})", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Requête invalide.");

        return determineRedirectUrl(request);
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public String handleInvalidCurrentPassword(InvalidCurrentPasswordException ex,
                                               jakarta.servlet.http.HttpServletRequest request,
                                               RedirectAttributes redirectAttributes) {
        logger.warn("Invalid current password for path={}", request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Mot de passe actuel incorrect.");
        return determineRedirectUrl(request);
    }

    /**
     * Fallback for any unhandled exceptions to prevent application crashes
     * and provide user-friendly feedback. (NPE, SQLExceptions, etc.)
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        logger.error("Unexpected error at {}: ", request.getRequestURI(), ex);

        redirectAttributes.addFlashAttribute("errorMessageFromGEH",
                "Une erreur inattendue s'est produite. Veuillez réessayer.");

        return determineRedirectUrl(request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFound(NoResourceFoundException ex,
                                        RedirectAttributes redirectAttributes) {

        logger.warn("Static resource not found: {}", ex.getMessage());

        redirectAttributes.addFlashAttribute("errorMessageFromGEH", "Page demandée introuvable.");
        return "redirect:/login";
    }

    /* ---------- Helpers ---------- */
    private String determineRedirectUrl(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (path.startsWith("/transactions")) {
            return "redirect:/transactions";
        } else if (path.startsWith("/contacts")) {
            return "redirect:/contacts";
        } else if (path.startsWith("/profile")) {
            return "redirect:/profile";
        } else if (path.startsWith("/register")) {
            return "redirect:/register";
        } else if (path.startsWith("/login")) {
            return "redirect:/login";
        }

        // Redirect authenticated users to /transactions by default to avoid
        // having the root path ("/") processed as a static resource request.
        return "redirect:/transactions";
    }
}

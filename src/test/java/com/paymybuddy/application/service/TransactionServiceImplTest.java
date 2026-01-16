package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.Transaction;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.infrastructure.repository.AccountRepository;
import com.paymybuddy.infrastructure.repository.TransactionRepository;
import com.paymybuddy.infrastructure.repository.UserContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserContactRepository userContactRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        User senderUser = User.create("Sender", "sender@email.com", "password");
        User receiverUser = User.create("Receiver", "receiver@email.com", "password");

        senderAccount = Account.create(senderUser);
        receiverAccount = Account.create(receiverUser);

        senderAccount.deposit(new BigDecimal("200.00"));
    }

/* Tests pour la méthode transfer */
    @Test
    void transfer_shouldDebitSenderCredit_AndSaveTransaction() {
        long senderUserId = 1L;
        long receiverUserId = 2L;
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Dinner";

        when(accountRepository.findById(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findById(receiverUserId)).thenReturn(Optional.of(receiverAccount));
        when(userContactRepository.existsByUser_IdAndContact_Id(senderUserId, receiverUserId)).thenReturn(true);
        // Mock the save method to return the transaction passed to it
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal senderBalanceBefore = senderAccount.getBalance();
        BigDecimal receiverBalanceBefore = receiverAccount.getBalance();

        // Act
        transactionService.transfer(senderUserId, receiverUserId, amount, description);

        // Assert (balances)
        BigDecimal expectedFee = feeOf(amount);
        BigDecimal expectedDebit = amount.add(expectedFee);

        assertThat(senderAccount.getBalance())
                .isEqualByComparingTo(senderBalanceBefore.subtract(expectedDebit));

        assertThat(receiverAccount.getBalance())
                .isEqualByComparingTo(receiverBalanceBefore.add(amount));

        // Assert (transaction saved)
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction saved = captor.getValue();
        assertThat(saved.getSenderAccount()).isSameAs(senderAccount);
        assertThat(saved.getReceiverAccount()).isSameAs(receiverAccount);
        assertThat(saved.getAmount()).isEqualByComparingTo(amount);
        assertThat(saved.getFee()).isEqualByComparingTo(expectedFee);
        assertThat(saved.getDescription()).isEqualTo(description);
        assertThat(saved.getDate()).isNotNull();

        // Assert (repository interactions)
        verify(accountRepository).findByUserId(senderUserId);
        verify(accountRepository).findByUserId(receiverUserId);
        verify(userContactRepository).existsByUser_IdAndContact_Id(senderUserId, receiverUserId);
        verifyNoMoreInteractions(accountRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_WhenAmountIsNull() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, null, "Dinner"))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(accountRepository, transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_WhenAmountIsNegativeOrZero() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("0.00"), "Dinner"))
                .isInstanceOf(RuntimeException.class);

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("-1.00"), "Dinner"))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(accountRepository, transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_WhenSenderAccountNotFound() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        when(accountRepository.findById(senderUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("5.00"), "Dinner"))
                .isInstanceOf(RuntimeException.class);

        verify(accountRepository).findById(senderUserId);
        verifyNoInteractions(transactionRepository, userContactRepository);
        verify(accountRepository, never()).findByUserId(receiverUserId);
    }

    @Test
    void transfer_shouldThrow_WhenReceiverAccountNotFound() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        when(accountRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findById(receiverUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("5.00"), "Dinner"))
                .isInstanceOf(RuntimeException.class);

        verify(accountRepository).findByUserId(senderUserId);
        verify(accountRepository).findById(receiverUserId);
        verifyNoInteractions(transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_WhenReceiverIsNotAContact() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        when(accountRepository.findById(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findById(receiverUserId)).thenReturn(Optional.of(receiverAccount));
        when(userContactRepository.existsByUser_IdAndContact_Id(senderUserId, receiverUserId)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("5.00"), "Dinner"))
                .isInstanceOf(RuntimeException.class);

        verify(userContactRepository).existsByUser_IdAndContact_Id(senderUserId, receiverUserId);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_shouldThrow_whenInsufficientBalanceInSenderAccount() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        // Balance after withdrawal is 0.50
        senderAccount.withdraw(new BigDecimal("199.50"));

        when(accountRepository.findById(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findById(receiverUserId)).thenReturn(Optional.of(receiverAccount));
        when(userContactRepository.existsByUser_IdAndContact_Id(senderUserId, receiverUserId)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("10.00"), "Dinner"))
                .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(transactionRepository);
    }

    /**
     * Fee = 0.5% (0.005) of the amount, rounded to 2 decimal places (HALF_UP).
     * Example: 100.00 -> 0.50
     */
    private static BigDecimal feeOf(BigDecimal amount) {
        return amount
                .multiply(new BigDecimal("0.005"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
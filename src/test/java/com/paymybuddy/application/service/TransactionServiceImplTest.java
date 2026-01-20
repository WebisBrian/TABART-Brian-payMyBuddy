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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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
    private TransactionServiceImpl transactionService;

    private Account senderAccount;
    private Account receiverAccount;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        User senderUser = User.create("Sender", "sender@email.com", "password");
        User receiverUser = User.create("Receiver", "receiver@email.com", "password");

        senderAccount = Account.create(senderUser);
        receiverAccount = Account.create(receiverUser);

        senderAccount.deposit(new BigDecimal("200.00"));

        fixedClock = Clock.fixed(
                Instant.parse("2026-01-16T10:15:30Z"),
                ZoneId.of("UTC")
        );

        transactionService = new TransactionServiceImpl(
                accountRepository,
                transactionRepository,
                userContactRepository,
                fixedClock
        );
    }

    /* ---------- transfer() ---------- */
    @Test
    void transfer_shouldDebitSenderCredit_andSaveTransaction() {
        long senderUserId = 1L;
        long receiverUserId = 2L;
        BigDecimal amount = new BigDecimal("100.00");
        String description = "Dinner";

        when(accountRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverUserId)).thenReturn(Optional.of(receiverAccount));
        when(userContactRepository.existsByUser_IdAndContact_Id(senderUserId, receiverUserId)).thenReturn(true);

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
        LocalDateTime expectedNow = LocalDateTime.ofInstant(fixedClock.instant(), fixedClock.getZone());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction saved = captor.getValue();
        assertThat(saved.getSenderAccount()).isSameAs(senderAccount);
        assertThat(saved.getReceiverAccount()).isSameAs(receiverAccount);
        assertThat(saved.getAmount()).isEqualByComparingTo(amount);
        assertThat(saved.getFee()).isEqualByComparingTo(expectedFee);
        assertThat(saved.getDescription()).isEqualTo(description);
        assertThat(saved.getDate()).isEqualTo(expectedNow);

        // Assert (repository interactions)
        verify(accountRepository).findByUserId(senderUserId);
        verify(accountRepository).findByUserId(receiverUserId);
        verify(userContactRepository).existsByUser_IdAndContact_Id(senderUserId, receiverUserId);
        verifyNoMoreInteractions(accountRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_whenSenderIdIsNull() {
        assertThatThrownBy(() -> transactionService.transfer(null, 2L, new BigDecimal("100.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_whenReceiverIdIsNull() {
        assertThatThrownBy(() -> transactionService.transfer(1L, null, new BigDecimal("100.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_whenAmountIsNull() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, null, "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_whenAmountIsNegativeOrZero() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("0.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("-1.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_whenSenderAccountNotFound() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        when(accountRepository.findByUserId(senderUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("5.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(accountRepository).findByUserId(senderUserId);
        verifyNoInteractions(transactionRepository, userContactRepository);
        verify(accountRepository, never()).findByUserId(receiverUserId);
    }

    @Test
    void transfer_shouldThrow_whenReceiverAccountNotFound() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        when(accountRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("5.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(accountRepository).findByUserId(senderUserId);
        verify(accountRepository).findByUserId(receiverUserId);
        verifyNoInteractions(transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_whenSenderAndContactAreSame() {
        long senderUserId = 1L;
        long receiverUserId = 1L;

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("5.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository, userContactRepository);
    }

    @Test
    void transfer_shouldThrow_whenReceiverIsNotAContact() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        when(accountRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverUserId)).thenReturn(Optional.of(receiverAccount));
        when(userContactRepository.existsByUser_IdAndContact_Id(senderUserId, receiverUserId)).thenReturn(false);

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("5.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userContactRepository).existsByUser_IdAndContact_Id(senderUserId, receiverUserId);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_shouldThrow_whenInsufficientBalanceInSenderAccount() {
        long senderUserId = 1L;
        long receiverUserId = 2L;

        // Balance after withdrawal is 0.50
        senderAccount.withdraw(new BigDecimal("199.50"));

        when(accountRepository.findByUserId(senderUserId)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserId(receiverUserId)).thenReturn(Optional.of(receiverAccount));
        when(userContactRepository.existsByUser_IdAndContact_Id(senderUserId, receiverUserId)).thenReturn(true);

        assertThatThrownBy(() -> transactionService.transfer(senderUserId, receiverUserId, new BigDecimal("10.00"), "Dinner"))
                .isInstanceOf(IllegalArgumentException.class);

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

    /* ---------- getTransaction() ---------- */
    @Test
    void getTransactionHistory_shouldReturnPagesTransactionsOrderedByDateDesc() {
        long userId = 1L;

        User user = User.create("User", "user@email.com", "password");
        Account account = Account.create(user);

        Transaction t1 = Transaction.create(
                account,
                account,
                new BigDecimal("10.00"),
                new BigDecimal("0.05"),
                LocalDateTime.of(2026, 1, 10, 10, 0),
                "Old transaction"
        );

        Transaction t2 = Transaction.create(
                account,
                account,
                new BigDecimal("20.00"),
                new BigDecimal("0.10"),
                LocalDateTime.of(2026, 1, 15, 12, 0),
                "Recent transaction"
        );

        Page<Transaction> page = new PageImpl<>(
                List.of(t2, t1),
                PageRequest.of(0, 10),
                2
        );

        when(accountRepository.findByUserId(userId))
                .thenReturn(Optional.of(account));

        when(transactionRepository.findTransactionHistory(account.getId(), PageRequest.of(0, 10)))
                .thenReturn(page);

        // Act
        Page<Transaction> result = transactionService.getTransactionHistory(userId, PageRequest.of(0, 10));

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Recent transaction");
        assertThat(result.getContent().get(1).getDescription()).isEqualTo("Old transaction");

        verify(accountRepository).findByUserId(userId);
        verify(transactionRepository).findTransactionHistory(account.getId(), PageRequest.of(0, 10));
    }

    @Test
    void getTransactionHistory_shouldIncludeSentAndReceivedTransactions() {
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = User.create("User", "user@email.com", "password");
        Account account = Account.create(user);

        Transaction sent = Transaction.create(
                account,
                account,
                new BigDecimal("10.00"),
                new BigDecimal("0.05"),
                LocalDateTime.now(),
                "Sent"
        );

        Transaction received = Transaction.create(
                account,
                account,
                new BigDecimal("20.00"),
                new BigDecimal("0.10"),
                LocalDateTime.now(),
                "Received"
        );

        Page<Transaction> page = new PageImpl<>(
                List.of(sent, received),
                pageable,
                2
        );

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(transactionRepository.findTransactionHistory(account.getId(), pageable)).thenReturn(page);

        Page<Transaction> result = transactionService.getTransactionHistory(userId, pageable);

        assertThat(result.getContent()).containsExactlyInAnyOrder(sent, received);
    }

    @Test
    void getTransactionHistory_shouldReturnEmptyPage_whenNoTransactions() {
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User user = User.create("User", "user@mail.com", "pwd");
        Account account = Account.create(user);

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(transactionRepository.findTransactionHistory(account.getId(), pageable))
                .thenReturn(Page.empty(pageable));

        Page<Transaction> result = transactionService.getTransactionHistory(userId, pageable);

        assertThat(result).isEmpty();
    }

    @Test
    void getTransactionHistory_shouldThrow_whenUserIdIsNull() {
        assertThatThrownBy(() -> transactionService.getTransactionHistory(null, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void getTransactionHistory_shouldThrow_whenPageableIsNull() {
        assertThatThrownBy(() -> transactionService.getTransactionHistory(1L, null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void getTransactionHistory_shouldThrow_whenUserAccountNotFound() {
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.getTransactionHistory(userId, pageable)
        ).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getTransactionHistory_shouldRespectPaginationParameters() {
        long userId = 1L;
        Pageable pageable = PageRequest.of(1, 5);

        User user = User.create("User", "user@mail.com", "pwd");
        Account account = Account.create(user);

        Page<Transaction> page = new PageImpl<>(
                List.of(),
                pageable,
                20
        );

        when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(account));
        when(transactionRepository.findTransactionHistory(account.getId(), pageable)).thenReturn(page);

        Page<Transaction> result = transactionService.getTransactionHistory(userId, pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
    }
}
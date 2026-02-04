package com.paymybuddy.web.mapper;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.Transaction;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.web.dto.TransactionRowDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionRowMapperTest {

    private final TransactionRowMapper mapper = Mappers.getMapper(TransactionRowMapper.class);

    @Test
    void toRowDto_shouldBeSent_andUseReceiverAsCounterparty_whenUserIsSender() {
        Transaction transaction = mock(Transaction.class);

        Account senderAccount = mock(Account.class);
        Account receiverAccount = mock(Account.class);

        User sender = mock(User.class);
        User receiver = mock(User.class);

        when(transaction.getSenderAccount()).thenReturn(senderAccount);
        when(transaction.getReceiverAccount()).thenReturn(receiverAccount);
        when(transaction.getAmount()).thenReturn(new BigDecimal("10.00"));

        when(senderAccount.getUser()).thenReturn(sender);
        when(receiverAccount.getUser()).thenReturn(receiver);

        when(sender.getId()).thenReturn(1L);
        when(receiver.getId()).thenReturn(2L);

        when(receiver.getUsername()).thenReturn("Jason");
        when(receiver.getEmail()).thenReturn("jason@email.com");

        // Act
        TransactionRowDto dto = mapper.toRowDto(transaction, 1L);

        // Assert
        assertEquals(TransactionRowDto.Direction.SENT, dto.direction());
        assertEquals("Envoyé", dto.directionLabel());
        assertEquals("Jason (jason@email.com)", dto.counterpartyLabel());
        assertEquals(new BigDecimal("-10.00"), dto.signedAmount());
    }

    @Test
    void toRowDto_shouldBeReceived_andUseSenderAsCounterparty_whenUserIsReceiver() {
        Transaction transaction = mock(Transaction.class);

        Account senderAccount = mock(Account.class);
        Account receiverAccount = mock(Account.class);

        User sender = mock(User.class);
        User receiver = mock(User.class);

        when(transaction.getSenderAccount()).thenReturn(senderAccount);
        when(transaction.getReceiverAccount()).thenReturn(receiverAccount);
        when(transaction.getAmount()).thenReturn(new BigDecimal("15.00"));

        when(senderAccount.getUser()).thenReturn(sender);
        when(receiverAccount.getUser()).thenReturn(receiver);

        when(sender.getId()).thenReturn(2L);
        when(receiver.getId()).thenReturn(1L);

        when(sender.getUsername()).thenReturn("Jenny");
        when(sender.getEmail()).thenReturn("jenny@mail.com");

        // Act
        TransactionRowDto dto = mapper.toRowDto(transaction, 1L);

        // Assert
        assertEquals(TransactionRowDto.Direction.RECEIVED, dto.direction());
        assertEquals("Reçu", dto.directionLabel());
        assertEquals("Jenny (jenny@mail.com)", dto.counterpartyLabel());
        assertEquals(new BigDecimal("15.00"), dto.signedAmount());
    }
}
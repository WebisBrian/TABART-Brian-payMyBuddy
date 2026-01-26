package com.paymybuddy.web.mapper;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.Transaction;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.web.dto.TransactionRowDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

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

        when(senderAccount.getUser()).thenReturn(sender);
        when(receiverAccount.getUser()).thenReturn(receiver);

        when(sender.getId()).thenReturn(1L);
        when(receiver.getId()).thenReturn(2L);

        when(receiver.getUserName()).thenReturn("Alice");
        when(receiver.getEmail()).thenReturn("alice@mail.com");

        // Act
        TransactionRowDto dto = mapper.toRowDto(transaction, 1L);

        // Assert
        assertEquals(TransactionRowDto.Direction.SENT, dto.direction());
        assertEquals("Alice (alice@mail.com)", dto.counterpartyLabel());
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

        when(senderAccount.getUser()).thenReturn(sender);
        when(receiverAccount.getUser()).thenReturn(receiver);

        when(sender.getId()).thenReturn(2L);
        when(receiver.getId()).thenReturn(1L);

        when(sender.getUserName()).thenReturn("Bob");
        when(sender.getEmail()).thenReturn("bob@mail.com");

        // Act
        TransactionRowDto dto = mapper.toRowDto(transaction, 1L);

        // Assert
        assertEquals(TransactionRowDto.Direction.RECEIVED, dto.direction());
        assertEquals("Bob (bob@mail.com)", dto.counterpartyLabel());
    }
}
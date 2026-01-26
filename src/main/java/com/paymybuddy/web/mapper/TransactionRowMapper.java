package com.paymybuddy.web.mapper;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.Transaction;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.web.dto.TransactionRowDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TransactionRowMapper {

    @Mapping(target = "direction",
            expression = "java(directionOf(transaction, userId))")
    @Mapping(target = "directionLabel",
            expression = "java(directionLabelOf(transaction, userId))")
    @Mapping(target = "counterpartyLabel",
            expression = "java(counterpartyLabelOf(transaction, userId))")
    @Mapping(target = "signedAmount",
            expression = "java(signedAmountOf(transaction, userId))")
    TransactionRowDto toRowDto(Transaction transaction, Long userId);

    /* Helpers methods */

    default TransactionRowDto.Direction directionOf(Transaction transaction, Long userId) {
        Long senderUserId = userIdOf(transaction.getSenderAccount());
        return senderUserId.equals(userId)
                ? TransactionRowDto.Direction.SENT
                : TransactionRowDto.Direction.RECEIVED;
    }

    default String directionLabelOf(Transaction transaction, Long userId) {
        return directionOf(transaction, userId) == TransactionRowDto.Direction.SENT
                ? "Envoyé"
                : "Reçu";
    }

    default String counterpartyLabelOf(Transaction transaction, Long userId) {
        Account sender = transaction.getSenderAccount();
        Account receiver = transaction.getReceiverAccount();

        User counterparty = userIdOf(sender).equals(userId)
                ? receiver.getUser()
                : sender.getUser();

        return counterparty.getUserName() + " (" + counterparty.getEmail() + ")";
    }

    default BigDecimal signedAmountOf(Transaction transaction, Long userId) {
        BigDecimal amount = transaction.getAmount();
        if (amount == null) {
            return null;
        }

        return directionOf(transaction, userId) == TransactionRowDto.Direction.SENT
                ? amount.negate()
                : amount;
    }

    default Long userIdOf(Account account) {
        return account.getUser().getId();
    }
}

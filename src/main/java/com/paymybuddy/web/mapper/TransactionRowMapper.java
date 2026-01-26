package com.paymybuddy.web.mapper;

import com.paymybuddy.domain.entity.Account;
import com.paymybuddy.domain.entity.Transaction;
import com.paymybuddy.domain.entity.User;
import com.paymybuddy.web.dto.TransactionRowDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionRowMapper {

    @Mapping(target = "direction",
            expression = "java(directionOf(transaction, userId))")
    @Mapping(target = "counterpartyLabel",
            expression = "java(counterpartyLabelOf(transaction, userId))")
    TransactionRowDto toRowDto(Transaction transaction, Long userId);

    /* Helpers methods */

    default TransactionRowDto.Direction directionOf(Transaction transaction, Long userId) {
        Long senderUserId = userIdOf(transaction.getSenderAccount());
        return senderUserId.equals(userId)
                ? TransactionRowDto.Direction.SENT
                : TransactionRowDto.Direction.RECEIVED;
    }

    default String counterpartyLabelOf(Transaction transaction, Long userId) {
        Account sender = transaction.getSenderAccount();
        Account receiver = transaction.getReceiverAccount();

        User counterparty = userIdOf(sender).equals(userId)
                ? receiver.getUser()
                : sender.getUser();

        return counterparty.getUserName() + " (" + counterparty.getEmail() + ")";
    }

    default Long userIdOf(Account account) {
        return account.getUser().getId();
    }
}

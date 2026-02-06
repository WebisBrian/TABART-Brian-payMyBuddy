package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"senderAccount", "receiverAccount"})
@EqualsAndHashCode(of = "id")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fee;

    @Column(nullable = false)
    private LocalDateTime date;

    private String description;

    // FK : sender_account_id -> accounts.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private Account senderAccount;

    // FK : receiver_account_id -> accounts.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private Account receiverAccount;

    // Private constructor
    private Transaction(Account senderAccount,
                       Account receiverAccount,
                       BigDecimal amount,
                       BigDecimal fee,
                       LocalDateTime date,
                       String description) {
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.fee = fee;
        this.date = date;
        this.description = description;
    }

    // Factory method
    public static Transaction create(Account senderAccount,
                                     Account receiverAccount,
                                     BigDecimal amount,
                                     BigDecimal fee,
                                     LocalDateTime date,
                                     String description) {
        if (senderAccount == null) {
            throw new MissingSenderAccountException();
        }
        if (receiverAccount == null) {
            throw new MissingReceiverAccountException();
        }
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidTransactionAmountException(amount);
        }
        if (fee == null || fee.signum() < 0) {
            throw new InvalidTransactionFeeException(fee);
        }
        if (date == null) {
            throw new MissingTransactionDateException();
        }

        return new Transaction(senderAccount, receiverAccount, amount, fee, date, description);
    }
}

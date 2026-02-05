package com.paymybuddy.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "user")
@EqualsAndHashCode(of = "id")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Balance is required.")
    @Column(nullable = false, precision=19, scale=2)
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Private constructor
    private Account(User user) {
        this.user = user;
    }

    // Factory method
    public static Account create(User user) {
        return new Account(user);
    }

    // Public Methods
    public void deposit(BigDecimal amount) {
        requirePositive(amount);
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        requirePositive(amount);
        this.balance = this.balance.subtract(amount);
    }

    // Private Methods
    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Le montant indiqué doit être positif.");
        }
    }
}

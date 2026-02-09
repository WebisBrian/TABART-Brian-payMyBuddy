package com.paymybuddy.domain.entity;

import com.paymybuddy.domain.exception.InvalidUserFieldException;
import com.paymybuddy.domain.utils.EmailNormalizer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "passwordHash")
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    private User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Factory method
    public static User create(String username, String email, String passwordHash) {
        return new User(
                requireValidUsername(username),
                EmailNormalizer.normalize(email),
                requireNonBlank(passwordHash, "Password")
        );
    }

    // Public methods
    public void changeUsername(String username) {
        this.username = requireValidUsername(username);
    }

    public void changeEmail(String email) {
        this.email = EmailNormalizer.normalize(email);
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = requireNonBlank(passwordHash, "Password");
    }

    // Private methods
    private static String requireNonBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidUserFieldException(field + " must not be null or blank.");
        }

        return value.trim();
    }

    private static String requireValidUsername(String username) {
        String trimmed = requireNonBlank(username, "Username");

        if (trimmed.length() > 100) {
            throw new InvalidUserFieldException("Username must not exceed 100 characters.");
        }

        return trimmed;
    }
}

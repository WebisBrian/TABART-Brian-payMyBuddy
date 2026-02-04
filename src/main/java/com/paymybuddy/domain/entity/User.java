package com.paymybuddy.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "passwordHash")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String passwordHash;

    // private constructor
    private User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // factory method
    public static User create(String username, String email, String passwordHash) {
        return new User(username, email, passwordHash);
    }

    // methods
    public void changeUsername(String username) {
        this.username = username;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}

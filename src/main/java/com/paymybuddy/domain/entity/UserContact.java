package com.paymybuddy.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_contacts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_contacts", columnNames = {"user_id", "contact_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "contact"})
@EqualsAndHashCode(of = {"user", "contact"})
public class UserContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK : user_id -> users(id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FK : contact_id -> users(id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private User contact;

    // Private constructor
    private UserContact(User user, User contact) {
        this.user = user;
        this.contact = contact;
    }

    // Factory method
    public static UserContact create(User user, User contact) {
        if (user == null || contact == null) {
            throw new IllegalArgumentException("User and contact must not be null.");
        }

        if (user == contact || (user.getId() != null && user.getId().equals(contact.getId()))) {
            throw new IllegalArgumentException("User cannot add himself as a contact.");
        }

        return new UserContact(user, contact);
    }
}

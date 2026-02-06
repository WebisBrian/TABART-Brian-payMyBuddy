package com.paymybuddy.domain.exception;

import com.paymybuddy.domain.entity.User;

public class MissingUserOrContactException extends RuntimeException {

    public MissingUserOrContactException(User user, User contact) {
        super("User or contact must not be null. Provided User: " + user + " / Provided Contact " + contact);
    }
}

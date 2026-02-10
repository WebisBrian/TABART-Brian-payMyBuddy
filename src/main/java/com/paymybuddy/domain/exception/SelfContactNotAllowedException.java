package com.paymybuddy.domain.exception;

import com.paymybuddy.domain.entity.User;

public class SelfContactNotAllowedException extends RuntimeException {

    public SelfContactNotAllowedException() {
        super("User cannot add himself as a contact.");
    }
}

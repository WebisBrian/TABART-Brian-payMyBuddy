package com.paymybuddy.application.service.exception;

public class NotInContactsException extends RuntimeException {

    public NotInContactsException(Long senderId, Long receiverId) {
        super("Users must be contacts to transfer money. Provided userId: " + senderId+ " and userId: " + receiverId + ".");
    }
}

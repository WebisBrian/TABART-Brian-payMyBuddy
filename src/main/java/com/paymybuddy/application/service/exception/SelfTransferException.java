package com.paymybuddy.application.service.exception;

public class SelfTransferException extends RuntimeException {
    public SelfTransferException(Long senderId, Long receiverId) {
        super("Self transfer not allowed. Provided userId: " + senderId + " / Provided contactId: " + receiverId + ".");
    }
}

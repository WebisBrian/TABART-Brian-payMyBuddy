package com.paymybuddy.application.service;

import java.math.BigDecimal;

public interface AccountService {

    BigDecimal getBalance(Long userId);

    void deposit(Long userId, BigDecimal amount);

    void withdraw(Long userId, BigDecimal amount);
}

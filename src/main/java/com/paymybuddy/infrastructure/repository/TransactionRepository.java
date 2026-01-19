package com.paymybuddy.infrastructure.repository;

import com.paymybuddy.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findBySenderAccount_IdOrderByDateDesc(Long accountId, Pageable pageable);

    Page<Transaction> findByReceiverAccount_IdOrderByDateDesc(Long accountId, Pageable pageable);

}

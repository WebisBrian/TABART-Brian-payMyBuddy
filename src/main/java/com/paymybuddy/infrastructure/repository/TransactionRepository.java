package com.paymybuddy.infrastructure.repository;

import com.paymybuddy.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findBySenderAccount_IdOrderByDateDesc(Long accountId, Pageable pageable);

    Page<Transaction> findByReceiverAccount_IdOrderByDateDesc(Long accountId, Pageable pageable);

    @Query(
            value = """
            select t
            from Transaction t
            where t.senderAccount.id = :accountId
               or t.receiverAccount.id = :accountId
            order by t.date desc
        """,
            countQuery = """
            select count(t)
            from Transaction t
            where t.senderAccount.id = :accountId
               or t.receiverAccount.id = :accountId
        """
    )
    Page<Transaction> findTransactionHistory(@Param("accountId") Long accountId, Pageable pageable);
}

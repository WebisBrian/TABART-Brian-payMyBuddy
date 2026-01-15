package com.paymybuddy.infrastructure.repository;

import com.paymybuddy.domain.entity.User;
import com.paymybuddy.domain.entity.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {

    boolean existsByUser_IdAndContact_Id(Long userId, Long contactId);

    List<UserContact> findAllByUser_Id(Long userId);

    @Query("select uc.contact from UserContact uc where uc.user.id = :userId")
    List<User> findContactsByUserId(@Param("userId") Long userId);

    void deletedByUser_IdAndContact_Id(Long userId, Long contactId);
}

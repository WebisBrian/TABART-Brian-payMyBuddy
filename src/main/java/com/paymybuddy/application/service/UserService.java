package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.User;

import java.util.List;

public interface UserService {

    void addContact(Long userId, Long contactId);

    void removeContact(Long userId, Long contactId);

    List<User> listContacts(Long userId);

}

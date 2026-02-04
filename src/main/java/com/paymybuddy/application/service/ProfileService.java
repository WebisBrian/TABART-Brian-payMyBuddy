package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.User;

public interface ProfileService {

    public void updateProfile(String email, String newUsername, String newEmail);

}

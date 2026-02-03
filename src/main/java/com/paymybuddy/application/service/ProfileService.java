package com.paymybuddy.application.service;

import com.paymybuddy.domain.entity.User;

public interface ProfileService {

    public void updateProfile(String email, String newUserName, String newEmail);

}

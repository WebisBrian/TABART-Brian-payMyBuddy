package com.paymybuddy.application.service;

public interface ProfileService {

    public void updateProfile(String email, String newUsername, String newEmail);

    public void changePassword(String currentEmail, String currentPassword, String newPassword);

}

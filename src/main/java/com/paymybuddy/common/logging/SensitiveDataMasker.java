package com.paymybuddy.common.logging;

public class SensitiveDataMasker {

    private SensitiveDataMasker() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "-";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }

        String firstChar = email.substring(0, 1);
        String domain = email.substring(atIndex);

        return firstChar + "***" + domain;
    }
}

package com.portfolio.banking.util;

import java.security.SecureRandom;

public final class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private AccountNumberGenerator() {}

    /**
     * Generates a 12-digit standardized bank account number.
     * Starts with '10' (bank branch identifier) followed by 10 random digits.
     */
    public static String generate() {
        StringBuilder sb = new StringBuilder("10");
        for (int i = 0; i < 10; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}

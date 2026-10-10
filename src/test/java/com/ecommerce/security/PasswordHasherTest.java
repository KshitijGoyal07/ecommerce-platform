package com.ecommerce.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test
    void hashesAndVerifiesCorrectPassword() {
        char[] password = "marketSquare!123".toCharArray();
        String encoded = PasswordHasher.hash(password);
        assertTrue(encoded.startsWith("pbkdf2-sha256$"));
        assertTrue(PasswordHasher.verify(password, encoded));
        Arrays.fill(password, '\0');
    }

    @Test
    void differentHashesUseDifferentSalts() {
        String first = PasswordHasher.hash("marketSquare!123".toCharArray());
        String second = PasswordHasher.hash("marketSquare!123".toCharArray());
        assertNotEquals(first, second);
    }

    @Test
    void rejectsIncorrectPassword() {
        String encoded = PasswordHasher.hash("marketSquare!123".toCharArray());
        assertFalse(PasswordHasher.verify("wrongPassword!".toCharArray(), encoded));
    }

    @Test
    void rejectsMalformedStoredHash() {
        assertFalse(PasswordHasher.verify("marketSquare!123".toCharArray(), "not-a-hash"));
        assertFalse(PasswordHasher.verify("marketSquare!123".toCharArray(), "pbkdf2-sha256$999999999$aa$bb"));
    }

    @Test
    void rejectsTooShortPasswordWhenHashing() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hash("short".toCharArray()));
    }
}

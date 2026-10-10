package com.ecommerce.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {
    @Test
    void normalizesEmailAndName() {
        UserAccount account = new UserAccount(1, "  Alex Buyer ", " ALEX@example.com ",
                Role.BUYER, true, Instant.parse("2026-01-01T00:00:00Z"));
        assertEquals("Alex Buyer", account.fullName());
        assertEquals("alex@example.com", account.email());
    }

    @Test
    void rejectsInvalidIdentity() {
        assertThrows(IllegalArgumentException.class, () -> new UserAccount(0, "Alex", "alex@example.com",
                Role.BUYER, true, Instant.EPOCH));
        assertThrows(IllegalArgumentException.class, () -> new UserAccount(1, "Alex", "bad-email",
                Role.BUYER, true, Instant.EPOCH));
    }

    @Test
    void requiresRoleAndCreationTime() {
        assertThrows(NullPointerException.class, () -> new UserAccount(1, "Alex", "alex@example.com",
                null, true, Instant.EPOCH));
        assertThrows(NullPointerException.class, () -> new UserAccount(1, "Alex", "alex@example.com",
                Role.BUYER, true, null));
    }
}

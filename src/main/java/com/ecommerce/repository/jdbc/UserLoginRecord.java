package com.ecommerce.repository.jdbc;

import com.ecommerce.model.UserAccount;

import java.util.Objects;

/** Internal authentication lookup. Never store this object in an HTTP session or return it to a view. */
public record UserLoginRecord(UserAccount account, String passwordHash) {
    public UserLoginRecord {
        Objects.requireNonNull(account, "Account is required.");
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash is required.");
        }
    }
}

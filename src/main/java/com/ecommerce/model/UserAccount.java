package com.ecommerce.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/** Safe database-facing view of a user account; deliberately excludes password hashes. */
public record UserAccount(
        long id,
        String fullName,
        String email,
        Role role,
        boolean active,
        Instant createdAt) {

    public UserAccount {
        if (id <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank.");
        }
        fullName = fullName.trim();
        if (email == null || !email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        email = email.trim().toLowerCase(Locale.ROOT);
        role = Objects.requireNonNull(role, "Role is required.");
        createdAt = Objects.requireNonNull(createdAt, "Creation time is required.");
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRoleName() {
        return role.name();
    }

    public boolean isActive() {
        return active;
    }
}

package com.ecommerce.service;

import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;
import com.ecommerce.repository.jdbc.UserDao;
import com.ecommerce.repository.jdbc.UserLoginRecord;
import com.ecommerce.security.PasswordHasher;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Objects;

/** Application service for buyer/seller registration and credential verification. */
public final class AuthenticationService {
    private final UserDao userDao;

    public AuthenticationService() {
        this(new UserDao());
    }

    public AuthenticationService(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "User DAO is required.");
    }

    /** Public registration deliberately excludes ADMIN; administrator accounts must be provisioned by an administrator. */
    public UserAccount register(String fullName, String email, String password, String requestedRole)
            throws SQLException {
        String normalizedName = requireText(fullName, "Full name");
        String normalizedEmail = requireText(email, "Email").toLowerCase(Locale.ROOT);
        String normalizedRole = requireText(requestedRole, "Account type").toUpperCase(Locale.ROOT);
        Role role;
        try {
            role = Role.valueOf(normalizedRole);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Choose either Buyer or Seller account type.");
        }
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Administrator accounts cannot be created through public registration.");
        }
        if (password == null || password.length() < 10) {
            throw new IllegalArgumentException("Password must contain at least 10 characters.");
        }
        char[] passwordChars = password.toCharArray();
        String hash;
        try {
            hash = PasswordHasher.hash(passwordChars);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
        return userDao.create(normalizedName, normalizedEmail, hash, role);
    }

    /** Returns empty for an unknown, inactive, or incorrectly authenticated account. */
    public Optional<UserAccount> authenticate(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        Optional<UserLoginRecord> found = userDao.findCredentialsByEmail(email);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        UserLoginRecord loginRecord = found.get();
        if (!loginRecord.account().active()) {
            return Optional.empty();
        }
        char[] passwordChars = password.toCharArray();
        try {
            return PasswordHasher.verify(passwordChars, loginRecord.passwordHash())
                    ? Optional.of(loginRecord.account()) : Optional.empty();
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        return value.trim();
    }
}

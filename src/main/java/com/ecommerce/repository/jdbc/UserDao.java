package com.ecommerce.repository.jdbc;

import com.ecommerce.db.DatabaseConnection;
import com.ecommerce.model.Role;
import com.ecommerce.model.UserAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Objects;

/** JDBC data-access object for MarketSquare user accounts. */
public final class UserDao {

    /**
     * Creates an account using an already-computed password hash, never a plaintext password.
     * Password hashing and login verification are added in the authentication checkpoint.
     */
    public UserAccount create(String fullName, String email, String passwordHash, Role role) throws SQLException {
        String normalizedName = requireText(fullName, "Full name");
        String normalizedEmail = normalizeEmail(email);
        String safeHash = requireText(passwordHash, "Password hash");
        Role safeRole = Objects.requireNonNull(role, "Role is required.");
        String sql = "INSERT INTO users (full_name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, normalizedName);
            statement.setString(2, normalizedEmail);
            statement.setString(3, safeHash);
            statement.setString(4, safeRole.name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("MySQL did not return a generated user ID.");
                }
                long id = keys.getLong(1);
                return findById(id).orElseThrow(
                        () -> new SQLException("Created user account could not be reloaded."));
            }
        }
    }

    public Optional<UserAccount> findById(long userId) throws SQLException {
        requirePositiveId(userId);
        String sql = "SELECT user_id, full_name, email, role, is_active, created_at FROM users WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapAccount(resultSet)) : Optional.empty();
            }
        }
    }

    public Optional<UserAccount> findByEmail(String email) throws SQLException {
        String normalizedEmail = normalizeEmail(email);
        String sql = "SELECT user_id, full_name, email, role, is_active, created_at FROM users WHERE email = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedEmail);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapAccount(resultSet)) : Optional.empty();
            }
        }
    }

    public List<UserAccount> findAllActive() throws SQLException {
        String sql = "SELECT user_id, full_name, email, role, is_active, created_at "
                + "FROM users WHERE is_active = TRUE ORDER BY user_id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<UserAccount> accounts = new ArrayList<>();
            while (resultSet.next()) {
                accounts.add(mapAccount(resultSet));
            }
            return List.copyOf(accounts);
        }
    }

    /** Soft-deactivates an account so that historical orders remain linked to it. */
    public boolean deactivate(long userId) throws SQLException {
        requirePositiveId(userId);
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ? AND is_active = TRUE";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            return statement.executeUpdate() == 1;
        }
    }

    private static UserAccount mapAccount(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new UserAccount(
                resultSet.getLong("user_id"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                Role.valueOf(resultSet.getString("role")),
                resultSet.getBoolean("is_active"),
                createdAt == null ? Instant.EPOCH : createdAt.toInstant());
    }

    private static String normalizeEmail(String email) {
        String normalized = requireText(email, "Email").toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        return normalized;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.trim();
    }

    private static void requirePositiveId(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
    }
}


package com.ecommerce.repository.jdbc;

import com.ecommerce.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Database operations for a buyer's saved addresses and payment methods. */
public final class BuyerAccountDao {

    public List<Map<String, Object>> findAddresses(long userId)
            throws SQLException {
        requirePositiveId(userId, "User ID");

        String sql = """
                SELECT address_id, address_label, recipient_name, phone,
                       address_line1, address_line2, city, state_region,
                       postal_code, country, is_default
                FROM user_addresses
                WHERE user_id = ?
                ORDER BY is_default DESC, address_id DESC
                """;

        List<Map<String, Object>> addresses = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> address = new LinkedHashMap<>();
                    address.put("id", rs.getLong("address_id"));
                    address.put("label", rs.getString("address_label"));
                    address.put("recipientName", rs.getString("recipient_name"));
                    address.put("phone", rs.getString("phone"));
                    address.put("line1", rs.getString("address_line1"));
                    address.put("line2", rs.getString("address_line2"));
                    address.put("city", rs.getString("city"));
                    address.put("state", rs.getString("state_region"));
                    address.put("postalCode", rs.getString("postal_code"));
                    address.put("country", rs.getString("country"));
                    address.put("isDefault", rs.getBoolean("is_default"));
                    addresses.add(address);
                }
            }
        }

        return List.copyOf(addresses);
    }

    public List<Map<String, Object>> findPaymentMethods(long userId)
            throws SQLException {
        requirePositiveId(userId, "User ID");

        String sql = """
                SELECT payment_method_id, method_type, display_label,
                       last_four, is_default
                FROM payment_methods
                WHERE user_id = ?
                ORDER BY is_default DESC, payment_method_id DESC
                """;

        List<Map<String, Object>> methods = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> method = new LinkedHashMap<>();
                    method.put("id", rs.getLong("payment_method_id"));
                    method.put("type", rs.getString("method_type"));
                    method.put("label", rs.getString("display_label"));
                    method.put("lastFour", rs.getString("last_four"));
                    method.put("isDefault", rs.getBoolean("is_default"));
                    methods.add(method);
                }
            }
        }

        return List.copyOf(methods);
    }

    public void addAddress(
            long userId,
            String label,
            String recipientName,
            String phone,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country) throws SQLException {

        requirePositiveId(userId, "User ID");
        requireText(label, "Address label", 50);
        requireText(recipientName, "Recipient name", 120);
        requireText(phone, "Phone", 30);
        requireText(line1, "Address line 1", 255);
        requireText(city, "City", 100);
        requireText(state, "State", 100);
        requireText(postalCode, "Postal code", 20);

        if (line2 != null && line2.length() > 255) {
            throw new IllegalArgumentException(
                    "Address line 2 must be at most 255 characters.");
        }

        if (country == null || country.isBlank()) {
            country = "India";
        }
        requireText(country, "Country", 100);

        String sql = """
                INSERT INTO user_addresses
                    (user_id, address_label, recipient_name, phone,
                     address_line1, address_line2, city, state_region,
                     postal_code, country, is_default)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean makeDefault;

            try (PreparedStatement count = connection.prepareStatement(
                    "SELECT COUNT(*) FROM user_addresses WHERE user_id = ?")) {
                count.setLong(1, userId);

                try (ResultSet rs = count.executeQuery()) {
                    rs.next();
                    makeDefault = rs.getLong(1) == 0;
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, userId);
                statement.setString(2, label.trim());
                statement.setString(3, recipientName.trim());
                statement.setString(4, phone.trim());
                statement.setString(5, line1.trim());

                if (line2 == null || line2.isBlank()) {
                    statement.setNull(6, Types.VARCHAR);
                } else {
                    statement.setString(6, line2.trim());
                }

                statement.setString(7, city.trim());
                statement.setString(8, state.trim());
                statement.setString(9, postalCode.trim());
                statement.setString(10, country.trim());
                statement.setBoolean(11, makeDefault);
                statement.executeUpdate();
            }
        }
    }

    public boolean deleteAddress(long userId, long addressId)
            throws SQLException {
        requirePositiveId(userId, "User ID");
        requirePositiveId(addressId, "Address ID");

        String sql = """
                DELETE FROM user_addresses
                WHERE address_id = ? AND user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, addressId);
            statement.setLong(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean setDefaultAddress(long userId, long addressId)
            throws SQLException {
        requirePositiveId(userId, "User ID");
        requirePositiveId(addressId, "Address ID");

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement reset = connection.prepareStatement(
                        "UPDATE user_addresses SET is_default = FALSE WHERE user_id = ?")) {
                    reset.setLong(1, userId);
                    reset.executeUpdate();
                }

                int updated;
                try (PreparedStatement select = connection.prepareStatement(
                        "UPDATE user_addresses SET is_default = TRUE "
                                + "WHERE address_id = ? AND user_id = ?")) {
                    select.setLong(1, addressId);
                    select.setLong(2, userId);
                    updated = select.executeUpdate();
                }

                if (updated != 1) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void addPaymentMethod(
            long userId,
            String type,
            String label,
            String lastFour) throws SQLException {

        requirePositiveId(userId, "User ID");
        requireText(label, "Payment method label", 100);

        String normalizedType = type == null
                ? ""
                : type.trim().toUpperCase(java.util.Locale.ROOT);

        if (!Set.of("CARD", "UPI", "COD", "OTHER").contains(normalizedType)) {
            throw new IllegalArgumentException(
                    "Choose a supported payment method.");
        }

        if (lastFour != null && !lastFour.isBlank()
                && !lastFour.trim().matches("\\d{4}")) {
            throw new IllegalArgumentException(
                    "Last four digits must contain exactly four numbers.");
        }

        if (!"CARD".equals(normalizedType)
                && lastFour != null && !lastFour.isBlank()) {
            throw new IllegalArgumentException(
                    "Only card methods can have last-four digits.");
        }

        String sql = """
                INSERT INTO payment_methods
                    (user_id, method_type, display_label,
                     provider_token_reference, last_four, is_default)
                VALUES (?, ?, ?, NULL, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean makeDefault;

            try (PreparedStatement count = connection.prepareStatement(
                    "SELECT COUNT(*) FROM payment_methods WHERE user_id = ?")) {
                count.setLong(1, userId);

                try (ResultSet rs = count.executeQuery()) {
                    rs.next();
                    makeDefault = rs.getLong(1) == 0;
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, userId);
                statement.setString(2, normalizedType);
                statement.setString(3, label.trim());

                if (lastFour == null || lastFour.isBlank()) {
                    statement.setNull(4, Types.CHAR);
                } else {
                    statement.setString(4, lastFour.trim());
                }

                statement.setBoolean(5, makeDefault);
                statement.executeUpdate();
            }
        }
    }

    public boolean deletePaymentMethod(long userId, long paymentMethodId)
            throws SQLException {
        requirePositiveId(userId, "User ID");
        requirePositiveId(paymentMethodId, "Payment method ID");

        String sql = """
                DELETE FROM payment_methods
                WHERE payment_method_id = ? AND user_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, paymentMethodId);
            statement.setLong(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean setDefaultPaymentMethod(long userId, long paymentMethodId)
            throws SQLException {
        requirePositiveId(userId, "User ID");
        requirePositiveId(paymentMethodId, "Payment method ID");

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement reset = connection.prepareStatement(
                        "UPDATE payment_methods SET is_default = FALSE WHERE user_id = ?")) {
                    reset.setLong(1, userId);
                    reset.executeUpdate();
                }

                int updated;
                try (PreparedStatement select = connection.prepareStatement(
                        "UPDATE payment_methods SET is_default = TRUE "
                                + "WHERE payment_method_id = ? AND user_id = ?")) {
                    select.setLong(1, paymentMethodId);
                    select.setLong(2, userId);
                    updated = select.executeUpdate();
                }

                if (updated != 1) {
                    connection.rollback();
                    return false;
                }

                connection.commit();
                return true;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void requirePositiveId(long id, String field) {
        if (id <= 0) {
            throw new IllegalArgumentException(field + " must be positive.");
        }
    }

    private static void requireText(
            String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }

        if (value.trim().length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " must be at most " + maxLength + " characters.");
        }
    }
}

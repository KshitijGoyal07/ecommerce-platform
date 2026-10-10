package com.ecommerce.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Small command-line smoke check for the local MySQL connection. */
public final class DatabaseHealthCheck {
    private DatabaseHealthCheck() {
    }

    public static void main(String[] args) {
        String sql = "SELECT DATABASE() AS database_name, VERSION() AS server_version";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                System.out.println("MySQL connection successful.");
                System.out.println("Database: " + resultSet.getString("database_name"));
                System.out.println("Server version: " + resultSet.getString("server_version"));
            } else {
                System.err.println("Connected to MySQL, but the health-check query returned no row.");
                System.exit(1);
            }
        } catch (SQLException | IllegalStateException exception) {
            System.err.println("MySQL connection check failed: " + exception.getMessage());
            System.exit(1);
        }
    }
}

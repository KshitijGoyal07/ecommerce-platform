package com.ecommerce.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Creates JDBC connections using environment variables rather than committed credentials. */
public final class DatabaseConnection {
    public static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/marketsquare?serverTimezone=UTC";

    private DatabaseConnection() {
        // Utility class.
    }

    /**
     * Opens a connection to MarketSquare's MySQL database.
     *
     * <p>Required environment variables: MARKETSQUARE_DB_USER and
     * MARKETSQUARE_DB_PASSWORD. MARKETSQUARE_DB_URL is optional and defaults
     * to the local development database.</p>
     */
    public static Connection getConnection() throws SQLException {
        String user = requireEnvironmentVariable("MARKETSQUARE_DB_USER");
        String password = requireEnvironmentVariable("MARKETSQUARE_DB_PASSWORD");
        String url = System.getenv().getOrDefault("MARKETSQUARE_DB_URL", DEFAULT_URL);

        if (url.isBlank()) {
            throw new IllegalStateException("MARKETSQUARE_DB_URL must not be blank.");
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String requireEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required environment variable " + name + " is not set. "
                            + "Follow docs/jdbc-and-product-persistence.md to configure it.");
        }
        return value;
    }
}

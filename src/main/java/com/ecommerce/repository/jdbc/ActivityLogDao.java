
package com.ecommerce.repository.jdbc;

import com.ecommerce.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** JDBC persistence for marketplace activity logs. */
public class ActivityLogDao {

    /**
     * Records an activity performed by an authenticated application user.
     * The actor ID must refer to an existing users.user_id.
     */
    public void record(
            long actorUserId,
            String action,
            String entityType,
            Long entityId) throws SQLException {

        if (actorUserId <= 0) {
            throw new IllegalArgumentException("Actor user ID must be positive.");
        }
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Activity action is required.");
        }
        if (action.length() > 100) {
            throw new IllegalArgumentException("Activity action is too long.");
        }
        if (entityType != null && entityType.length() > 100) {
            throw new IllegalArgumentException("Entity type is too long.");
        }
        if (entityId != null && entityId <= 0) {
            throw new IllegalArgumentException("Entity ID must be positive.");
        }

        String sql = """
                INSERT INTO system_activity_logs
                    (actor_user_id, action, entity_type, entity_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, actorUserId);
            statement.setString(2, action);

            if (entityType == null || entityType.isBlank()) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, entityType);
            }

            if (entityId == null) {
                statement.setNull(4, Types.BIGINT);
            } else {
                statement.setLong(4, entityId);
            }

            statement.executeUpdate();
        }
    }

    /** Returns recent activity, newest first. */
    public List<ActivityLogEntry> findRecent(int limit) throws SQLException {
        if (limit < 1 || limit > 200) {
            throw new IllegalArgumentException(
                    "Activity limit must be between 1 and 200.");
        }

        String sql = """
                SELECT l.activity_id,
                       l.actor_user_id,
                       COALESCE(u.full_name, 'Unknown or deleted user') AS actor_name,
                       COALESCE(u.email, '') AS actor_email,
                       l.action,
                       l.entity_type,
                       l.entity_id,
                       l.created_at
                FROM system_activity_logs l
                LEFT JOIN users u ON u.user_id = l.actor_user_id
                ORDER BY l.created_at DESC, l.activity_id DESC
                LIMIT ?
                """;

        List<ActivityLogEntry> entries = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    long actorValue = resultSet.getLong("actor_user_id");
                    Long actorId = resultSet.wasNull() ? null : actorValue;

                    long entityValue = resultSet.getLong("entity_id");
                    Long entityId = resultSet.wasNull() ? null : entityValue;

                    Timestamp timestamp = resultSet.getTimestamp("created_at");
                    Instant createdAt = timestamp == null
                            ? Instant.EPOCH
                            : timestamp.toInstant();

                    entries.add(new ActivityLogEntry(
                            resultSet.getLong("activity_id"),
                            actorId,
                            resultSet.getString("actor_name"),
                            resultSet.getString("actor_email"),
                            resultSet.getString("action"),
                            resultSet.getString("entity_type"),
                            entityId,
                            createdAt
                    ));
                }
            }
        }

        return List.copyOf(entries);
    }
}

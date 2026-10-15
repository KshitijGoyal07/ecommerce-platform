
package com.ecommerce.repository.jdbc;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Read-only view of a marketplace activity log entry. */
public record ActivityLogEntry(
        long activityId,
        Long actorUserId,
        String actorName,
        String actorEmail,
        String action,
        String entityType,
        Long entityId,
        Instant createdAt) {

    public long getActivityId() {
        return activityId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getActorName() {
        return actorName;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getCreatedAtDisplay() {
        return DateTimeFormatter
                .ofPattern("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH)
                .withZone(ZoneId.systemDefault())
                .format(createdAt);
    }
}

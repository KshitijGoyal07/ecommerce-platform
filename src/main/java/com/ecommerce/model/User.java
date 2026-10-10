package com.ecommerce.model;

/** Common state and behavior shared by every MarketSquare user. */
public abstract class User implements DashboardAccess {
    private final long id;
    private final String fullName;
    private final String email;
    private final Role role;

    protected User(long id, String fullName, String email, Role role) {
        if (id <= 0) {
            throw new IllegalArgumentException("User ID must be positive.");
        }
        this.id = id;
        this.fullName = requireText(fullName, "Full name");
        this.email = validateEmail(email);
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.trim();
    }

    private static String validateEmail(String email) {
        String normalized = requireText(email, "Email");
        if (!normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        return normalized.toLowerCase();
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", fullName='" + fullName + "', role=" + role + "}";
    }
}

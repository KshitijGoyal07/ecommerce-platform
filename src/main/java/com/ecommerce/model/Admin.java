package com.ecommerce.model;

/** An administrator who oversees users, listings, and marketplace orders. */
public final class Admin extends User {
    public Admin(long id, String fullName, String email) {
        super(id, fullName, email, Role.ADMIN);
    }

    @Override
    public String getDashboardPath() {
        return "/admin/dashboard";
    }

    @Override
    public String getWorkspaceDescription() {
        return "Oversee users, products, orders, and system activity.";
    }
}

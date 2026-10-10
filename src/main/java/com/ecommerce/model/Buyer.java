package com.ecommerce.model;

/** A customer who browses products and places orders. */
public final class Buyer extends User {
    public Buyer(long id, String fullName, String email) {
        super(id, fullName, email, Role.BUYER);
    }

    @Override
    public String getDashboardPath() {
        return "/buyer/dashboard";
    }

    @Override
    public String getWorkspaceDescription() {
        return "Browse products, manage a wishlist, and track orders.";
    }
}

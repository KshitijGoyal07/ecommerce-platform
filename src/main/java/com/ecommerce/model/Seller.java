package com.ecommerce.model;

/** A marketplace seller who manages listings and inventory. */
public final class Seller extends User {
    private final String storeName;

    public Seller(long id, String fullName, String email, String storeName) {
        super(id, fullName, email, Role.SELLER);
        this.storeName = requireText(storeName, "Store name");
    }

    public String getStoreName() {
        return storeName;
    }

    @Override
    public String getDashboardPath() {
        return "/seller/dashboard";
    }

    @Override
    public String getWorkspaceDescription() {
        return "Manage product listings, stock levels, and sales orders.";
    }
}

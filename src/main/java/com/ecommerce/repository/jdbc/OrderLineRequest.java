package com.ecommerce.repository.jdbc;

/** A requested product and quantity supplied when placing an order. */
public record OrderLineRequest(long productId, int quantity) {
    public OrderLineRequest {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero.");
        }
    }
}

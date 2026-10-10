package com.ecommerce.repository.jdbc;

import com.ecommerce.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Read-only order view reconstructed from the orders and order_items tables. */
public record PersistedOrder(
        long id,
        long buyerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddressSnapshot,
        Instant createdAt,
        List<PersistedOrderItem> items) {

    public PersistedOrder {
        if (id <= 0 || buyerId <= 0) {
            throw new IllegalArgumentException("Order ID and buyer ID must be positive.");
        }
        status = Objects.requireNonNull(status, "Order status is required.");
        totalAmount = Objects.requireNonNull(totalAmount, "Order total is required.");
        if (totalAmount.signum() < 0) {
            throw new IllegalArgumentException("Order total cannot be negative.");
        }
        if (shippingAddressSnapshot == null || shippingAddressSnapshot.isBlank()) {
            throw new IllegalArgumentException("Shipping address snapshot must not be blank.");
        }
        shippingAddressSnapshot = shippingAddressSnapshot.trim();
        createdAt = Objects.requireNonNull(createdAt, "Creation time is required.");
        items = List.copyOf(Objects.requireNonNull(items, "Order items are required."));
    }
}

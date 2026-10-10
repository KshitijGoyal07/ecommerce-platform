package com.ecommerce.model;

/** Lifecycle states for a marketplace order. */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

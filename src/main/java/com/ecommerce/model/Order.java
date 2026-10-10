package com.ecommerce.model;

import com.ecommerce.exception.InvalidOrderStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An order placed by a buyer, containing price snapshots for its items. */
public final class Order {
    private final long id;
    private final long buyerId;
    private final List<OrderItem> items;
    private final Instant createdAt;
    private OrderStatus status;

    public Order(long id, long buyerId, List<OrderItem> items) {
        if (id <= 0 || buyerId <= 0) {
            throw new IllegalArgumentException("Order ID and buyer ID must be positive.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item.");
        }
        if (items.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Order items cannot contain null values.");
        }
        this.id = id;
        this.buyerId = buyerId;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.createdAt = Instant.now();
        this.status = OrderStatus.PLACED;
    }

    public long getId() {
        return id;
    }

    public long getBuyerId() {
        return buyerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null || !isTransitionAllowed(status, newStatus)) {
            throw new InvalidOrderStatusException(status, newStatus);
        }
        status = newStatus;
    }

    private boolean isTransitionAllowed(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PLACED -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}

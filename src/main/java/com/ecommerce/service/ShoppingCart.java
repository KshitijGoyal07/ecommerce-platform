package com.ecommerce.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Session-scoped in-memory cart. Prices and stock are always rechecked during checkout. */
public final class ShoppingCart implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MAX_QUANTITY_PER_PRODUCT = 99;
    private final Map<Long, Integer> quantities = new LinkedHashMap<>();

    public synchronized void add(long productId, int quantity) {
        requirePositiveId(productId);
        requireValidQuantity(quantity);
        int current = quantities.getOrDefault(productId, 0);
        if (current + quantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new IllegalArgumentException("You can add up to 99 units of a product to your cart.");
        }
        quantities.put(productId, current + quantity);
    }

    public synchronized void setQuantity(long productId, int quantity) {
        requirePositiveId(productId);
        requireValidQuantity(quantity);
        if (!quantities.containsKey(productId)) {
            throw new IllegalArgumentException("That product is not in your cart.");
        }
        quantities.put(productId, quantity);
    }

    public synchronized boolean remove(long productId) {
        requirePositiveId(productId);
        return quantities.remove(productId) != null;
    }

    public synchronized Map<Long, Integer> getQuantities() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(quantities));
    }

    public synchronized int getTotalQuantity() {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }

    public synchronized boolean isEmpty() {
        return quantities.isEmpty();
    }

    public synchronized void clear() {
        quantities.clear();
    }

    private static void requirePositiveId(long productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive.");
        }
    }

    private static void requireValidQuantity(int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99.");
        }
    }
}

package com.ecommerce.model;

import java.math.BigDecimal;

/** A purchase-time snapshot of one product within an order. */
public final class OrderItem {
    private final long productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(long productId, String productName, int quantity, BigDecimal unitPrice) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive.");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero.");
        }
        this.productId = productId;
        this.productName = productName.trim();
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

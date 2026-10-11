package com.ecommerce.repository.jdbc;

import java.math.BigDecimal;
import java.util.Objects;

/** Read-only order-line view loaded from MySQL, including the purchase-time price. */
public record PersistedOrderItem(
        long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {

    public PersistedOrderItem {
        if (productId <= 0 || quantity <= 0) {
            throw new IllegalArgumentException("Product ID and quantity must be positive.");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank.");
        }
        productName = productName.trim();
        unitPrice = Objects.requireNonNull(unitPrice, "Unit price is required.");
        lineTotal = Objects.requireNonNull(lineTotal, "Line total is required.");
        if (unitPrice.signum() < 0 || lineTotal.signum() < 0) {
            throw new IllegalArgumentException("Order prices cannot be negative.");
        }
    }

    // JavaBean-style accessors for JSP Expression Language.
    public long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineTotal() { return lineTotal; }
}

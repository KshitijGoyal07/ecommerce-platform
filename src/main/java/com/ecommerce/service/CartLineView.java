package com.ecommerce.service;

import com.ecommerce.model.Product;

import java.math.BigDecimal;
import java.util.Objects;

/** View model for rendering a product and its quantity in the buyer's cart. */
public final class CartLineView {
    private final Product product;
    private final int quantity;
    private final BigDecimal lineTotal;

    public CartLineView(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "Product is required.");
        if (quantity < 1) {
            throw new IllegalArgumentException("Cart quantity must be positive.");
        }
        this.quantity = quantity;
        this.lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}

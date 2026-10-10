package com.ecommerce.service;

import com.ecommerce.model.Product;

import java.util.Objects;

/**
 * Coordinates inventory changes for products in the catalog.
 * Product's synchronized stock methods make each reserve/restock operation atomic
 * for all callers that operate on the same Product instance.
 */
public class InventoryService {

    private final ProductCatalog productCatalog;

    public InventoryService(ProductCatalog productCatalog) {
        this.productCatalog = Objects.requireNonNull(
                productCatalog, "Product catalog must not be null.");
    }

    /**
     * Reserves the requested quantity and returns the remaining stock.
     * Throws IllegalArgumentException for an unknown product ID and
     * InsufficientStockException when stock is not available.
     */
    public int reserveStock(long productId, int quantity) {
        Product product = findProduct(productId);
        product.reserveStock(quantity);
        return product.getStockQuantity();
    }

    /** Adds stock and returns the updated quantity. */
    public int restock(long productId, int quantity) {
        Product product = findProduct(productId);
        product.restock(quantity);
        return product.getStockQuantity();
    }

    /** Reads the current stock value. */
    public int getAvailableStock(long productId) {
        return findProduct(productId).getStockQuantity();
    }

    private Product findProduct(long productId) {
        return productCatalog.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No product exists with ID " + productId + "."));
    }
}

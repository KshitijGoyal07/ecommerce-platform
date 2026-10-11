package com.ecommerce.model;

import com.ecommerce.exception.InsufficientStockException;
import java.math.BigDecimal;

/** A product listed by a seller on the marketplace. */
public class Product {
    private final long id;
    private final long sellerId;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private int stockQuantity;
    private boolean active;
    private String imagePath;

    public Product(long id, long sellerId, String name, String description,
                   String category, BigDecimal price, int stockQuantity) {
        this(id, sellerId, name, description, category, price, stockQuantity, null);
    }

    public Product(long id, long sellerId, String name, String description,
                   String category, BigDecimal price, int stockQuantity, String imagePath) {
        if (id < 0 || sellerId <= 0) {
            throw new IllegalArgumentException("Product ID cannot be negative and seller ID must be positive.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        this.id = id;
        updateDetails(name, description, category, price);
        this.stockQuantity = stockQuantity;
        this.imagePath = normalizeImagePath(imagePath);
        this.active = true;
    }

    /** Returns the database ID, or 0 while this product is a new, unsaved draft. */
    public long getId() {
        return id;
    }

    /** Creates a new product draft. Persist it with ProductDao.create() to receive a database ID. */
    public static Product draft(long sellerId, String name, String description,
                                String category, BigDecimal price, int stockQuantity) {
        return new Product(0, sellerId, name, description, category, price, stockQuantity);
    }

    public long getSellerId() {
        return sellerId;
    }

    /** Optional stored image filename; null means this listing has no photo. */
    public synchronized String getImagePath() {
        return imagePath;
    }

    /** Stores only a generated image filename, never an arbitrary path or URL. */
    public synchronized void setImagePath(String imagePath) {
        this.imagePath = normalizeImagePath(imagePath);
    }

    private static String normalizeImagePath(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return null;
        String value = imagePath.trim();
        if (!value.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png)")) {
            throw new IllegalArgumentException("Product image filename is invalid.");
        }
        return value;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized String getCategory() {
        return category;
    }

    public synchronized BigDecimal getPrice() {
        return price;
    }

    public synchronized int getStockQuantity() {
        return stockQuantity;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void updateDetails(String name, String description, String category, BigDecimal price) {
        this.name = requireText(name, "Product name");
        this.description = description == null ? "" : description.trim();
        this.category = requireText(category, "Product category");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero.");
        }
        this.price = price;
    }

    /** Reduces available stock or throws a domain-specific exception. */
    public synchronized void reserveStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (!active) {
            throw new IllegalStateException("An inactive product cannot be purchased.");
        }
        if (quantity > stockQuantity) {
            throw new InsufficientStockException(quantity, stockQuantity);
        }
        stockQuantity -= quantity;
    }

    public synchronized void restock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than zero.");
        }
        stockQuantity = Math.addExact(stockQuantity, quantity);
    }

    public synchronized void deactivate() {
        active = false;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.trim();
    }
}

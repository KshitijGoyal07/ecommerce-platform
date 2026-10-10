package com.ecommerce.service;

import com.ecommerce.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Validated input for creating or editing a seller product listing. Monetary values are INR. */
public record ProductListingInput(
        String name,
        String description,
        String category,
        BigDecimal price) {

    public ProductListingInput {
        name = requiredText(name, "Product name", 180);
        description = description == null ? "" : description.trim();
        if (description.length() > 5000) {
            throw new IllegalArgumentException("Description must be 5,000 characters or fewer.");
        }
        category = requiredText(category, "Category", 100);
        price = validatePrice(price);
    }

    public static ProductListingInput fromForm(String name, String description, String category, String priceText) {
        if (priceText == null || priceText.isBlank()) {
            throw new IllegalArgumentException("Price in INR is required.");
        }
        final BigDecimal price;
        try {
            price = new BigDecimal(priceText.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid price in INR, using digits and an optional decimal point.");
        }
        return new ProductListingInput(name, description, category, price);
    }

    public Product toDraft(long sellerId, int stockQuantity) {
        if (sellerId <= 0) {
            throw new IllegalArgumentException("Seller account is invalid.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        return Product.draft(sellerId, name, description, category, price, stockQuantity);
    }

    public void applyTo(Product product) {
        Objects.requireNonNull(product, "Product is required.")
                .updateDetails(name, description, category, price);
    }

    public static int parseStockQuantity(String stockText) {
        if (stockText == null || stockText.isBlank()) {
            throw new IllegalArgumentException("Stock quantity is required.");
        }
        final int quantity;
        try {
            quantity = Integer.parseInt(stockText.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Stock quantity must be a whole number between 0 and 2,147,483,647.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        return quantity;
    }

    private static String requiredText(String value, String label, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(label + " must be " + maxLength + " characters or fewer.");
        }
        return trimmed;
    }

    private static BigDecimal validatePrice(BigDecimal candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Price in INR is required.");
        }
        final BigDecimal normalized;
        try {
            normalized = candidate.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Price must have no more than two decimal places.");
        }
        if (normalized.signum() <= 0) {
            throw new IllegalArgumentException("Price must be greater than ₹0.00.");
        }
        if (normalized.precision() > 12) {
            throw new IllegalArgumentException("Price must fit the supported INR range (up to ₹9,999,999,999.99).");
        }
        return normalized;
    }
}

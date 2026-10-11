package com.ecommerce.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductDraftTest {
    @Test
    void draftUsesZeroAsUnsavedId() {
        Product product = Product.draft(7, "Desk Lamp", "Warm LED lamp", "Home", 
                new BigDecimal("799.00"), 12);

        assertEquals(0, product.getId());
        assertEquals(7, product.getSellerId());
    }

    @Test
    void descriptionMayBeEmptyBecauseDatabaseDescriptionIsOptional() {
        Product product = Product.draft(7, "Desk Lamp", "", "Home",
                new BigDecimal("799.00"), 12);

        assertEquals("", product.getDescription());
    }

    @Test
    void newProductPhotoIsOptional() {
        Product product = Product.draft(7, "Desk Lamp", "Warm LED lamp", "Home",
                new BigDecimal("799.00"), 12);
        assertNull(product.getImagePath());
    }

    @Test
    void productPhotoOnlyAcceptsGeneratedImageFilenames() {
        Product product = Product.draft(7, "Desk Lamp", "Warm LED lamp", "Home",
                new BigDecimal("799.00"), 12);
        product.setImagePath("f47ac10b-58cc-4372-a567-0e02b2c3d479.png");
        assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479.png", product.getImagePath());
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> product.setImagePath("../private-file.png"));
    }

    @Test
    void negativeProductIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Product(-1, 7,
                "Desk Lamp", "Warm LED lamp", "Home", new BigDecimal("799.00"), 12));
    }
}

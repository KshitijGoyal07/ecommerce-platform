package com.ecommerce.service;

import com.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductCatalogTest {

    private ProductCatalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new ProductCatalog();
        catalog.addProduct(product(1, "Wireless Mouse", "Electronics", "799.00"));
        catalog.addProduct(product(2, "Coffee Mug", "Home", "299.00"));
        catalog.addProduct(product(3, "Mechanical Keyboard", "Electronics", "2499.00"));
    }

    private Product product(long id, String name, String category, String price) {
        return new Product(id, 10, name, "A sample product", category,
                new BigDecimal(price), 10);
    }

    @Test
    void searchIsCaseInsensitiveAndMatchesPartialNames() {
        List<Product> results = catalog.searchByName("MOUSE");

        assertEquals(1, results.size());
        assertEquals("Wireless Mouse", results.get(0).getName());
    }

    @Test
    void categoryFilterIsCaseInsensitive() {
        assertEquals(2, catalog.findByCategory("electronics").size());
    }

    @Test
    void priceRangeIncludesItsBoundaries() {
        List<Product> results = catalog.findWithinPriceRange(
                new BigDecimal("299.00"), new BigDecimal("799.00"));

        assertEquals(2, results.size());
    }

    @Test
    void inactiveProductsAreNotShownInBrowsingSearches() {
        catalog.findById(1L).orElseThrow().deactivate();

        assertTrue(catalog.searchByName("Mouse").isEmpty());
        assertEquals(2, catalog.findActiveProducts().size());
        assertEquals(3, catalog.countProducts());
    }

    @Test
    void addingDuplicateProductIdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> catalog.addProduct(product(1, "Duplicate", "Home", "10.00")));
    }
}

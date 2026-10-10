package com.ecommerce.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductListingInputTest {
    @Test
    void trimsTextAndNormalizesPriceToTwoDecimals() {
        ProductListingInput input = ProductListingInput.fromForm(
                "  Headphones  ", "  Wireless over-ear  ", "  Electronics  ", "1299.9");
        assertEquals("Headphones", input.name());
        assertEquals("Wireless over-ear", input.description());
        assertEquals("Electronics", input.category());
        assertEquals(new BigDecimal("1299.90"), input.price());
    }

    @Test
    void allowsAnEmptyDescription() {
        ProductListingInput input = ProductListingInput.fromForm("Notebook", " ", "Stationery", "49.00");
        assertEquals("", input.description());
    }

    @Test
    void rejectsMissingProductName() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductListingInput.fromForm("  ", "description", "Category", "10.00"));
    }

    @Test
    void rejectsOverlongCategory() {
        String category = "c".repeat(101);
        assertThrows(IllegalArgumentException.class,
                () -> ProductListingInput.fromForm("Product", "", category, "10.00"));
    }

    @Test
    void rejectsMalformedPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductListingInput.fromForm("Product", "", "Category", "twelve rupees"));
    }

    @Test
    void rejectsZeroOrNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductListingInput.fromForm("Product", "", "Category", "0.00"));
        assertThrows(IllegalArgumentException.class,
                () -> ProductListingInput.fromForm("Product", "", "Category", "-1.00"));
    }

    @Test
    void rejectsPricesWithMoreThanTwoNonZeroFractionDigits() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductListingInput.fromForm("Product", "", "Category", "10.125"));
    }

    @Test
    void rejectsPricesOutsideDatabasePrecision() {
        assertThrows(IllegalArgumentException.class,
                () -> ProductListingInput.fromForm("Product", "", "Category", "10000000000.00"));
    }

    @Test
    void parsesZeroAndPositiveStockQuantities() {
        assertEquals(0, ProductListingInput.parseStockQuantity("0"));
        assertEquals(23, ProductListingInput.parseStockQuantity(" 23 "));
    }

    @Test
    void rejectsNegativeOrNonIntegerStock() {
        assertThrows(IllegalArgumentException.class, () -> ProductListingInput.parseStockQuantity("-1"));
        assertThrows(IllegalArgumentException.class, () -> ProductListingInput.parseStockQuantity("1.5"));
        assertThrows(IllegalArgumentException.class, () -> ProductListingInput.parseStockQuantity(""));
    }

    @Test
    void createsDraftForTheAuthenticatedSeller() {
        ProductListingInput input = ProductListingInput.fromForm("Mouse", "USB mouse", "Electronics", "599.00");
        var draft = input.toDraft(42, 7);
        assertEquals(0, draft.getId());
        assertEquals(42, draft.getSellerId());
        assertEquals("Mouse", draft.getName());
        assertEquals(new BigDecimal("599.00"), draft.getPrice());
        assertEquals(7, draft.getStockQuantity());
    }
}

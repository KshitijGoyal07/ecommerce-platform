package com.ecommerce.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest {
    @Test
    void addAccumulatesQuantitiesAndReportsTotal() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(12, 2);
        cart.add(12, 3);
        cart.add(24, 1);

        assertEquals(Map.of(12L, 5, 24L, 1), cart.getQuantities());
        assertEquals(6, cart.getTotalQuantity());
        assertFalse(cart.isEmpty());
    }

    @Test
    void setQuantityChangesOnlyAnExistingLine() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(12, 2);
        cart.setQuantity(12, 4);
        assertEquals(4, cart.getQuantities().get(12L));
        assertThrows(IllegalArgumentException.class, () -> cart.setQuantity(99, 1));
    }

    @Test
    void removeReportsWhetherALineWasPresent() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(12, 1);
        assertTrue(cart.remove(12));
        assertFalse(cart.remove(12));
        assertTrue(cart.isEmpty());
    }

    @Test
    void clearRemovesEveryLine() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(12, 2);
        cart.add(24, 3);
        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getTotalQuantity());
    }

    @Test
    void returnedMapCannotMutateTheCart() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(12, 1);
        Map<Long, Integer> snapshot = cart.getQuantities();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put(24L, 1));
        assertEquals(Map.of(12L, 1), cart.getQuantities());
    }

    @Test
    void rejectsNonPositiveProductIds() {
        ShoppingCart cart = new ShoppingCart();
        assertThrows(IllegalArgumentException.class, () -> cart.add(0, 1));
        assertThrows(IllegalArgumentException.class, () -> cart.add(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> cart.remove(0));
    }

    @Test
    void rejectsInvalidLineQuantities() {
        ShoppingCart cart = new ShoppingCart();
        assertThrows(IllegalArgumentException.class, () -> cart.add(12, 0));
        assertThrows(IllegalArgumentException.class, () -> cart.add(12, -1));
        assertThrows(IllegalArgumentException.class, () -> cart.setQuantity(12, 0));
        assertThrows(IllegalArgumentException.class, () -> cart.setQuantity(12, 100));
    }

    @Test
    void rejectsCombinedQuantityOverTheLimit() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(12, 60);
        assertThrows(IllegalArgumentException.class, () -> cart.add(12, 40));
        assertEquals(60, cart.getQuantities().get(12L));
    }

    @Test
    void removesOnlyTheRequestedProduct() {
        ShoppingCart cart = new ShoppingCart();
        cart.add(12, 2);
        cart.add(24, 4);
        cart.remove(12);
        assertEquals(Map.of(24L, 4), cart.getQuantities());
        assertEquals(4, cart.getTotalQuantity());
    }

    @Test
    void cartStartsEmpty() {
        ShoppingCart cart = new ShoppingCart();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getTotalQuantity());
        assertTrue(cart.getQuantities().isEmpty());
    }
}

package com.ecommerce.repository.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderLineRequestTest {
    @Test
    void acceptsPositiveProductAndQuantity() {
        assertEquals(new OrderLineRequest(7, 2), new OrderLineRequest(7, 2));
    }

    @Test
    void rejectsInvalidProductIdAndQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new OrderLineRequest(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new OrderLineRequest(7, 0));
        assertThrows(IllegalArgumentException.class, () -> new OrderLineRequest(7, -1));
    }
}

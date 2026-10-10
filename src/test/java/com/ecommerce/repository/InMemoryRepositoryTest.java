package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {

    private Product sampleProduct(long id, String name) {
        return new Product(id, 7, name, "Test description", "Accessories",
                new BigDecimal("19.99"), 5);
    }

    @Test
    void savesAndFindsAnEntityById() {
        Repository<Product, Long> repository = new InMemoryRepository<>(Product::getId);
        Product product = sampleProduct(1, "Desk Lamp");

        repository.save(product);

        assertTrue(repository.findById(1L).isPresent());
        assertEquals("Desk Lamp", repository.findById(1L).orElseThrow().getName());
        assertEquals(1, repository.count());
    }

    @Test
    void savingAnExistingIdReplacesTheStoredEntity() {
        Repository<Product, Long> repository = new InMemoryRepository<>(Product::getId);
        repository.save(sampleProduct(1, "Old Name"));
        repository.save(sampleProduct(1, "Updated Name"));

        assertEquals(1, repository.count());
        assertEquals("Updated Name", repository.findById(1L).orElseThrow().getName());
    }

    @Test
    void findAllReturnsAReadOnlySnapshot() {
        Repository<Product, Long> repository = new InMemoryRepository<>(Product::getId);
        repository.save(sampleProduct(1, "Desk Lamp"));

        assertThrows(UnsupportedOperationException.class, () -> repository.findAll().clear());
        assertEquals(1, repository.count());
    }

    @Test
    void deleteReturnsWhetherAnEntityWasRemoved() {
        Repository<Product, Long> repository = new InMemoryRepository<>(Product::getId);
        repository.save(sampleProduct(1, "Desk Lamp"));

        assertTrue(repository.deleteById(1L));
        assertFalse(repository.deleteById(1L));
        assertEquals(0, repository.count());
    }
}

package com.ecommerce.service;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryServiceConcurrencyTest {

    @Test
    void simultaneousReservationsNeverOversellAvailableStock() throws Exception {
        int startingStock = 100;
        int reservationAttempts = 200;

        ProductCatalog catalog = new ProductCatalog();
        catalog.addProduct(new Product(1, 10, "Wireless Mouse", "Test product",
                "Electronics", new BigDecimal("799.00"), startingStock));
        InventoryService inventoryService = new InventoryService(catalog);

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicInteger successfulReservations = new AtomicInteger();
        AtomicInteger rejectedReservations = new AtomicInteger();
        List<Future<?>> tasks = new ArrayList<>();

        try {
            for (int i = 0; i < reservationAttempts; i++) {
                tasks.add(executor.submit(() -> {
                    try {
                        startSignal.await();
                        inventoryService.reserveStock(1, 1);
                        successfulReservations.incrementAndGet();
                    } catch (InsufficientStockException exception) {
                        rejectedReservations.incrementAndGet();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Reservation task was interrupted.", exception);
                    }
                }));
            }

            startSignal.countDown();
            for (Future<?> task : tasks) {
                task.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS),
                    "Executor should terminate after the test.");
        }

        assertEquals(startingStock, successfulReservations.get(),
                "Exactly the units in stock should be reserved successfully.");
        assertEquals(reservationAttempts - startingStock, rejectedReservations.get(),
                "All attempts beyond available stock should be rejected.");
        assertEquals(0, inventoryService.getAvailableStock(1),
                "Stock must never fall below zero.");
    }

    @Test
    void concurrentRestockingPreservesEveryIncrement() throws Exception {
        int restockOperations = 100;
        ProductCatalog catalog = new ProductCatalog();
        catalog.addProduct(new Product(2, 10, "Desk Lamp", "Test product",
                "Home", new BigDecimal("499.00"), 0));
        InventoryService inventoryService = new InventoryService(catalog);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<?>> tasks = new ArrayList<>();

        try {
            for (int i = 0; i < restockOperations; i++) {
                tasks.add(executor.submit(() -> {
                    try {
                        startSignal.await();
                        inventoryService.restock(2, 1);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Restock task was interrupted.", exception);
                    }
                }));
            }

            startSignal.countDown();
            for (Future<?> task : tasks) {
                task.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS),
                    "Executor should terminate after the test.");
        }

        assertEquals(restockOperations, inventoryService.getAvailableStock(2));
    }
}

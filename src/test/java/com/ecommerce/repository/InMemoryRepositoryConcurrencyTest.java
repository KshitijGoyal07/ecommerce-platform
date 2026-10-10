package com.ecommerce.repository;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryConcurrencyTest {

    @Test
    void concurrentWritesKeepAllUniqueEntities() throws Exception {
        int workerCount = 8;
        int productsPerWorker = 100;
        Repository<Product, Long> repository = new InMemoryRepository<>(Product::getId);
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<?>> tasks = new ArrayList<>();

        try {
            for (int worker = 0; worker < workerCount; worker++) {
                final int workerNumber = worker;
                tasks.add(executor.submit(() -> {
                    try {
                        startSignal.await();
                        for (int item = 0; item < productsPerWorker; item++) {
                            long id = (long) workerNumber * productsPerWorker + item + 1;
                            repository.save(new Product(id, 1, "Product " + id,
                                    "Concurrent repository test", "Testing",
                                    new BigDecimal("1.00"), 1));
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Repository task was interrupted.", exception);
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

        int expectedCount = workerCount * productsPerWorker;
        assertEquals(expectedCount, repository.count());
        assertEquals(expectedCount, repository.findAll().size());
    }
}

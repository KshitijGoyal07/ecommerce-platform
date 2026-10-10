package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.repository.InMemoryRepository;
import com.ecommerce.repository.Repository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Business operations for browsing and managing listed products.
 * The repository is injected so the in-memory implementation can later be
 * replaced with a JDBC-backed implementation without changing catalog callers.
 */
public class ProductCatalog {

    private final Repository<Product, Long> productRepository;

    /** Creates a catalog backed by temporary in-memory storage. */
    public ProductCatalog() {
        this(new InMemoryRepository<>(Product::getId));
    }

    /** Creates a catalog using the supplied repository implementation. */
    public ProductCatalog(Repository<Product, Long> productRepository) {
        this.productRepository = Objects.requireNonNull(
                productRepository, "Product repository must not be null.");
    }

    /** Adds a product, rejecting duplicate IDs rather than silently replacing a listing. */
    public Product addProduct(Product product) {
        Objects.requireNonNull(product, "Product must not be null.");
        if (productRepository.existsById(product.getId())) {
            throw new IllegalArgumentException(
                    "A product with ID " + product.getId() + " already exists.");
        }
        return productRepository.save(product);
    }

    /** Returns one product by ID, if it exists. */
    public Optional<Product> findById(long productId) {
        return productRepository.findById(productId);
    }

    /** Returns all products in insertion order, including inactive listings. */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /** Returns active listings sorted alphabetically by product name. */
    public List<Product> findActiveProducts() {
        return productRepository.findAll().stream()
                .filter(Product::isActive)
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Searches active product names; a null or blank search returns no results. */
    public List<Product> searchByName(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return List.of();
        }
        String query = searchTerm.trim().toLowerCase();
        return findActiveProducts().stream()
                .filter(product -> product.getName().toLowerCase().contains(query))
                .toList();
    }

    /** Returns active products in the requested category, ignoring letter case. */
    public List<Product> findByCategory(String category) {
        if (category == null || category.isBlank()) {
            return List.of();
        }
        String normalizedCategory = category.trim();
        return findActiveProducts().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase(normalizedCategory))
                .toList();
    }

    /** Returns active products whose prices fall within the inclusive range. */
    public List<Product> findWithinPriceRange(BigDecimal minimum, BigDecimal maximum) {
        Objects.requireNonNull(minimum, "Minimum price must not be null.");
        Objects.requireNonNull(maximum, "Maximum price must not be null.");
        if (minimum.compareTo(BigDecimal.ZERO) < 0 || maximum.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price range cannot contain negative values.");
        }
        if (minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("Minimum price cannot exceed maximum price.");
        }
        return findActiveProducts().stream()
                .filter(product -> product.getPrice().compareTo(minimum) >= 0)
                .filter(product -> product.getPrice().compareTo(maximum) <= 0)
                .toList();
    }

    /** Removes a listing from this repository; returns false when it does not exist. */
    public boolean removeProduct(long productId) {
        return productRepository.deleteById(productId);
    }

    public long countProducts() {
        return productRepository.count();
    }
}

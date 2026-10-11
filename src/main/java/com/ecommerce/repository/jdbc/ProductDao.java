package com.ecommerce.repository.jdbc;

import com.ecommerce.db.DatabaseConnection;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC data-access object for product listings and their inventory. */
public class ProductDao {
    private static final String SELECT_PRODUCT = """
            SELECT p.product_id,
                   p.seller_id,
                   p.name,
                   COALESCE(p.description, '') AS description,
                   COALESCE(c.name, 'Uncategorized') AS category_name,
                   p.price,
                   p.stock_quantity,
                   p.is_active,
                   p.image_path
            FROM products p
            LEFT JOIN categories c ON c.category_id = p.category_id
            """;

    /** Creates a new database product from a draft whose ID is zero. */
    public Product create(Product draft) throws SQLException {
        if (draft == null) {
            throw new IllegalArgumentException("Product must not be null.");
        }
        if (draft.getId() != 0) {
            throw new IllegalArgumentException("Only a new product draft (ID 0) can be created.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long categoryId = findOrCreateCategory(connection, draft.getCategory());
                String sql = """
                        INSERT INTO products
                            (seller_id, category_id, name, description, price, stock_quantity, is_active, image_path)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                long generatedId;
                try (PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setLong(1, draft.getSellerId());
                    statement.setLong(2, categoryId);
                    statement.setString(3, draft.getName());
                    setNullableDescription(statement, 4, draft.getDescription());
                    statement.setBigDecimal(5, draft.getPrice());
                    statement.setInt(6, draft.getStockQuantity());
                    statement.setBoolean(7, draft.isActive());
                    statement.setString(8, draft.getImagePath());
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("MySQL did not return a generated product ID.");
                        }
                        generatedId = keys.getLong(1);
                    }
                }
                connection.commit();
                return copyWithId(draft, generatedId);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /** Finds a product by ID, including inactive listings. */
    public Optional<Product> findById(long productId) throws SQLException {
        requirePositiveId(productId, "Product ID");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT_PRODUCT + " WHERE p.product_id = ?")) {
            statement.setLong(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapProduct(resultSet)) : Optional.empty();
            }
        }
    }

    /** Returns every listing, including inactive listings, ordered by ID. */
    public List<Product> findAll() throws SQLException {
        return queryProducts(SELECT_PRODUCT + " ORDER BY p.product_id");
    }

    /** Returns active products for the buyer-facing catalogue. */
    public List<Product> findActiveProducts(int limit, int offset) throws SQLException {
        if (limit < 1 || limit > 500) {
            throw new IllegalArgumentException("Limit must be between 1 and 500.");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be negative.");
        }
        String sql = SELECT_PRODUCT + " WHERE p.is_active = TRUE ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapProducts(resultSet);
            }
        }
    }

    /** Returns all listings belonging to one seller. */
    public List<Product> findBySellerId(long sellerId) throws SQLException {
        requirePositiveId(sellerId, "Seller ID");
        return queryProducts(SELECT_PRODUCT + " WHERE p.seller_id = ? ORDER BY p.product_id", sellerId);
    }

    /** Updates listing details but intentionally leaves stock changes to the inventory methods. */
    public boolean updateDetails(Product product) throws SQLException {
        if (product == null || product.getId() <= 0) {
            throw new IllegalArgumentException("A persisted product is required for updates.");
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long categoryId = findOrCreateCategory(connection, product.getCategory());
                String sql = """
                        UPDATE products
                        SET category_id = ?, name = ?, description = ?, price = ?
                        WHERE product_id = ?
                        """;
                int affected;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, categoryId);
                    statement.setString(2, product.getName());
                    setNullableDescription(statement, 3, product.getDescription());
                    statement.setBigDecimal(4, product.getPrice());
                    statement.setLong(5, product.getId());
                    affected = statement.executeUpdate();
                }
                boolean exists = affected > 0 || existsById(connection, product.getId());
                connection.commit();
                return exists;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /**
     * Updates listing details only when the supplied seller owns the listing.
     * Stock is intentionally managed by setStockQuantityForSeller instead.
     */
    public boolean updateDetailsForSeller(Product product, long sellerId) throws SQLException {
        if (product == null || product.getId() <= 0) {
            throw new IllegalArgumentException("A saved product is required for updates.");
        }
        requirePositiveId(sellerId, "Seller ID");
        if (product.getSellerId() != sellerId) {
            return false;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!existsBySellerId(connection, product.getId(), sellerId)) {
                    connection.rollback();
                    return false;
                }
                long categoryId = findOrCreateCategory(connection, product.getCategory());
                String sql = """
                        UPDATE products
                        SET category_id = ?, name = ?, description = ?, price = ?, image_path = ?
                        WHERE product_id = ? AND seller_id = ?
                        """;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setLong(1, categoryId);
                    statement.setString(2, product.getName());
                    setNullableDescription(statement, 3, product.getDescription());
                    statement.setBigDecimal(4, product.getPrice());
                    statement.setString(5, product.getImagePath());
                    statement.setLong(6, product.getId());
                    statement.setLong(7, sellerId);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    /** Sets an exact stock count, scoped to the owning seller account. */
    public boolean setStockQuantityForSeller(long sellerId, long productId, int quantity) throws SQLException {
        requirePositiveId(sellerId, "Seller ID");
        requirePositiveId(productId, "Product ID");
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE products SET stock_quantity = ? WHERE product_id = ? AND seller_id = ?")) {
            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.setLong(3, sellerId);
            int affected = statement.executeUpdate();
            return affected > 0 || existsBySellerId(connection, productId, sellerId);
        }
    }

    /** Deactivates a listing only if it belongs to the specified seller. */
    public boolean deactivateForSeller(long sellerId, long productId) throws SQLException {
        requirePositiveId(sellerId, "Seller ID");
        requirePositiveId(productId, "Product ID");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE products SET is_active = FALSE WHERE product_id = ? AND seller_id = ?")) {
            statement.setLong(1, productId);
            statement.setLong(2, sellerId);
            int affected = statement.executeUpdate();
            return affected > 0 || existsBySellerId(connection, productId, sellerId);
        }
    }

    private boolean existsBySellerId(Connection connection, long productId, long sellerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM products WHERE product_id = ? AND seller_id = ?")) {
            statement.setLong(1, productId);
            statement.setLong(2, sellerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /**
     * Reactivates a listing only if it belongs to the specified seller.
     */
    public boolean activateForSeller(long sellerId, long productId)
            throws SQLException {
        requirePositiveId(sellerId, "Seller ID");
        requirePositiveId(productId, "Product ID");

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE products SET is_active = TRUE "
                            + "WHERE product_id = ? AND seller_id = ?")) {

            statement.setLong(1, productId);
            statement.setLong(2, sellerId);

            int affected = statement.executeUpdate();
            return affected > 0
                    || existsBySellerId(connection, productId, sellerId);
        }
    }

    /** Hides a product without deleting historical order references. */
    public boolean deactivate(long productId) throws SQLException {
        requirePositiveId(productId, "Product ID");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE products SET is_active = FALSE WHERE product_id = ?")) {
            statement.setLong(1, productId);
            int affected = statement.executeUpdate();
            return affected > 0 || existsById(productId);
        }
    }

    /** Atomically reserves stock in MySQL, so concurrent buyers cannot oversell the same units. */
    public void reserveStock(long productId, int quantity) throws SQLException {
        requirePositiveId(productId, "Product ID");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        String sql = """
                UPDATE products
                SET stock_quantity = stock_quantity - ?
                WHERE product_id = ? AND is_active = TRUE AND stock_quantity >= ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);
            if (statement.executeUpdate() == 1) {
                return;
            }
        }

        Optional<Product> current = findById(productId);
        if (current.isEmpty()) {
            throw new SQLException("Cannot reserve stock: product " + productId + " does not exist.");
        }
        Product product = current.get();
        if (!product.isActive()) {
            throw new IllegalStateException("Cannot reserve stock for an inactive product.");
        }
        throw new InsufficientStockException(quantity, product.getStockQuantity());
    }

    /** Adds stock to an existing product; the update is atomic at the database level. */
    public void restock(long productId, int quantity) throws SQLException {
        requirePositiveId(productId, "Product ID");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than zero.");
        }
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?")) {
            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            if (statement.executeUpdate() == 0 && !existsById(productId)) {
                throw new SQLException("Cannot restock: product " + productId + " does not exist.");
            }
        }
    }

    private List<Product> queryProducts(String sql, long... parameters) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setLong(i + 1, parameters[i]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapProducts(resultSet);
            }
        }
    }

    private List<Product> mapProducts(ResultSet resultSet) throws SQLException {
        List<Product> products = new ArrayList<>();
        while (resultSet.next()) {
            products.add(mapProduct(resultSet));
        }
        return products;
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product(
                resultSet.getLong("product_id"),
                resultSet.getLong("seller_id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("category_name"),
                resultSet.getBigDecimal("price"),
                resultSet.getInt("stock_quantity"),
                resultSet.getString("image_path"));
        if (!resultSet.getBoolean("is_active")) {
            product.deactivate();
        }
        return product;
    }

    private long findOrCreateCategory(Connection connection, String name) throws SQLException {
        String insertSql = """
                INSERT INTO categories (name) VALUES (?)
                ON DUPLICATE KEY UPDATE category_id = LAST_INSERT_ID(category_id)
                """;
        try (PreparedStatement statement = connection.prepareStatement(
                insertSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT category_id FROM categories WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }
        throw new SQLException("Unable to find or create product category: " + name);
    }

    private boolean existsById(long productId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return existsById(connection, productId);
        }
    }

    private boolean existsById(Connection connection, long productId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM products WHERE product_id = ?")) {
            statement.setLong(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void setNullableDescription(PreparedStatement statement, int index, String description)
            throws SQLException {
        if (description == null || description.isBlank()) {
            statement.setNull(index, java.sql.Types.VARCHAR);
        } else {
            statement.setString(index, description.trim());
        }
    }

    private static Product copyWithId(Product product, long id) {
        Product persisted = new Product(id, product.getSellerId(), product.getName(), product.getDescription(),
                product.getCategory(), product.getPrice(), product.getStockQuantity(), product.getImagePath());
        if (!product.isActive()) {
            persisted.deactivate();
        }
        return persisted;
    }

    private static void requirePositiveId(long id, String fieldName) {
        if (id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }
}

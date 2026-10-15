package com.ecommerce.repository.jdbc;

import com.ecommerce.db.DatabaseConnection;
import com.ecommerce.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** JDBC persistence for a buyer's saved products. */
public final class WishlistDao {
    private static final String PRODUCT_COLUMNS = """
            SELECT p.product_id, p.seller_id, p.name,
                   COALESCE(p.description, '') AS description,
                   COALESCE(c.name, 'Uncategorized') AS category_name,
                   p.price, p.stock_quantity, p.is_active, p.image_path
            FROM wishlists w
            JOIN products p ON p.product_id = w.product_id
            LEFT JOIN categories c ON c.category_id = p.category_id
            """;

    /** Adds a product to a buyer's wishlist; returns false if it was already saved. */
    public boolean add(long buyerId, long productId) throws SQLException {
        requirePositiveId(buyerId, "Buyer ID");
        requirePositiveId(productId, "Product ID");
        String sql = """
                    INSERT IGNORE INTO wishlists (buyer_id, product_id, saved_price)
                    SELECT ?, p.product_id, p.price
                    FROM products p
                    WHERE p.product_id = ?
                    """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            statement.setLong(2, productId);
            return statement.executeUpdate() == 1;
        }
    }

    /** Removes only the specified product from the specified buyer's wishlist. */
    public boolean remove(long buyerId, long productId) throws SQLException {
        requirePositiveId(buyerId, "Buyer ID");
        requirePositiveId(productId, "Product ID");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM wishlists WHERE buyer_id = ? AND product_id = ?")) {
            statement.setLong(1, buyerId);
            statement.setLong(2, productId);
            return statement.executeUpdate() > 0;
        }
    }

    /** Lists a buyer's saved products, including inactive products so they can be removed. */
    public List<Product> findByBuyerId(long buyerId) throws SQLException {
        requirePositiveId(buyerId, "Buyer ID");
        String sql = PRODUCT_COLUMNS + " WHERE w.buyer_id = ? ORDER BY w.created_at DESC, w.wishlist_id DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (resultSet.next()) products.add(mapProduct(resultSet));
                return List.copyOf(products);
            }
        }
    }

     /** Returns the price captured when each product was added to a buyer's wishlist. */
    public Map<Long, BigDecimal> findSavedPricesByBuyerId(long buyerId)
            throws SQLException {

        requirePositiveId(buyerId, "Buyer ID");

        String sql = """
                SELECT product_id, saved_price
                FROM wishlists
                WHERE buyer_id = ?
                """;

        Map<Long, BigDecimal> prices = new HashMap<>();

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, buyerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    prices.put(
                            resultSet.getLong("product_id"),
                            resultSet.getBigDecimal("saved_price"));
                }
            }
        }

        return Map.copyOf(prices);
    }

    /** Returns the product IDs already saved by a buyer. */
    public Set<Long> findProductIdsByBuyerId(long buyerId) throws SQLException {
        requirePositiveId(buyerId, "Buyer ID");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT product_id FROM wishlists WHERE buyer_id = ?")) {
            statement.setLong(1, buyerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                Set<Long> ids = new HashSet<>();
                while (resultSet.next()) ids.add(resultSet.getLong(1));
                return Set.copyOf(ids);
            }
        }
    }

    private static Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product(
                resultSet.getLong("product_id"), resultSet.getLong("seller_id"),
                resultSet.getString("name"), resultSet.getString("description"),
                resultSet.getString("category_name"), resultSet.getBigDecimal("price"),
                resultSet.getInt("stock_quantity"), resultSet.getString("image_path"));
        if (!resultSet.getBoolean("is_active")) product.deactivate();
        return product;
    }

    private static void requirePositiveId(long id, String name) {
        if (id <= 0) throw new IllegalArgumentException(name + " must be positive.");
    }
}

package com.ecommerce.repository.jdbc;

import com.ecommerce.db.DatabaseConnection;
import com.ecommerce.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Records product detail views and returns each buyer's most recently viewed products. */
public final class ProductViewDao {
    /** Records a product detail view for the current buyer. */
    public void recordView(long buyerId, long productId) throws SQLException {
        requirePositiveId(buyerId, "Buyer ID");
        requirePositiveId(productId, "Product ID");
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO product_view_history (buyer_id, product_id) VALUES (?, ?)")) {
            statement.setLong(1, buyerId);
            statement.setLong(2, productId);
            statement.executeUpdate();
        }
    }

    /** Returns distinct active products, newest view first. */
    public List<Product> findRecentlyViewedByBuyerId(long buyerId, int limit) throws SQLException {
        requirePositiveId(buyerId, "Buyer ID");
        if (limit < 1 || limit > 20) throw new IllegalArgumentException("Limit must be between 1 and 20.");
        String sql = """
                WITH ranked_views AS (
                    SELECT product_id, viewed_at, view_id,
                           ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY viewed_at DESC, view_id DESC) AS rn
                    FROM product_view_history
                    WHERE buyer_id = ?
                )
                SELECT p.product_id, p.seller_id, p.name,
                       COALESCE(p.description, '') AS description,
                       COALESCE(c.name, 'Uncategorized') AS category_name,
                       p.price, p.stock_quantity, p.is_active
                FROM ranked_views rv
                JOIN products p ON p.product_id = rv.product_id
                LEFT JOIN categories c ON c.category_id = p.category_id
                WHERE rv.rn = 1 AND p.is_active = TRUE
                ORDER BY rv.viewed_at DESC, rv.view_id DESC
                LIMIT ?
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (resultSet.next()) products.add(mapProduct(resultSet));
                return List.copyOf(products);
            }
        }
    }

    private static Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product(
                resultSet.getLong("product_id"), resultSet.getLong("seller_id"),
                resultSet.getString("name"), resultSet.getString("description"),
                resultSet.getString("category_name"), resultSet.getBigDecimal("price"),
                resultSet.getInt("stock_quantity"));
        if (!resultSet.getBoolean("is_active")) product.deactivate();
        return product;
    }

    private static void requirePositiveId(long id, String name) {
        if (id <= 0) throw new IllegalArgumentException(name + " must be positive.");
    }
}


package com.ecommerce.repository.jdbc;

import com.ecommerce.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Seller-scoped order history and sales reports.
 *
 * Only order items belonging to the requested seller are returned.
 * Buyer delivery addresses and other sellers' items are never exposed.
 */
public final class SellerOrderReportDao {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault());

    /**
     * Returns orders containing this seller's products, newest first.
     * Each order contains only this seller's purchased items.
     */
    public List<SellerOrderView> findOrdersForSeller(long sellerId)
            throws SQLException {

        requirePositiveSellerId(sellerId);

        String sql = """
                SELECT
                    o.order_id,
                    o.status,
                    o.created_at,
                    p.name AS product_name,
                    oi.quantity,
                    oi.unit_price
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.order_id
                JOIN products p ON p.product_id = oi.product_id
                WHERE p.seller_id = ?
                ORDER BY o.created_at DESC,
                         o.order_id DESC,
                         oi.order_item_id ASC
                """;

        Map<Long, SellerOrderView> orders = new LinkedHashMap<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, sellerId);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    long orderId = results.getLong("order_id");

                    Timestamp timestamp = results.getTimestamp("created_at");
                    Instant createdAt = timestamp == null
                            ? Instant.EPOCH
                            : timestamp.toInstant();

                    String status = displayStatus(
                            results.getString("status"));

                    SellerOrderView order = orders.computeIfAbsent(
                            orderId,
                            id -> new SellerOrderView(id, status, createdAt));

                    BigDecimal unitPrice =
                            results.getBigDecimal("unit_price");

                    int quantity = results.getInt("quantity");

                    order.addItem(new SellerOrderItemView(
                            results.getString("product_name"),
                            quantity,
                            unitPrice));
                }
            }
        }

        return List.copyOf(orders.values());
    }

    /**
     * Summarizes non-cancelled orders containing this seller's products.
     * Sales value uses the prices recorded when the order was placed.
     */
    public SellerSalesSummary getSalesSummary(long sellerId)
            throws SQLException {

        requirePositiveSellerId(sellerId);

        String sql = """
                SELECT
                    COUNT(DISTINCT o.order_id) AS order_count,
                    COALESCE(SUM(oi.quantity), 0) AS units_sold,
                    COALESCE(
                        SUM(oi.quantity * oi.unit_price), 0.00
                    ) AS sales_total
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.order_id
                JOIN products p ON p.product_id = oi.product_id
                WHERE p.seller_id = ?
                  AND o.status <> 'CANCELLED'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, sellerId);

            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return new SellerSalesSummary(
                            results.getLong("order_count"),
                            results.getLong("units_sold"),
                            results.getBigDecimal("sales_total"));
                }
            }
        }

        return new SellerSalesSummary(
                0L, 0L, BigDecimal.ZERO);
    }

    private static String displayStatus(String databaseStatus) {
        if ("PENDING".equals(databaseStatus)) {
            return "PLACED";
        }

        return databaseStatus == null ? "UNKNOWN" : databaseStatus;
    }

    private static void requirePositiveSellerId(long sellerId) {
        if (sellerId <= 0) {
            throw new IllegalArgumentException(
                    "Seller ID must be positive.");
        }
    }

    public static final class SellerOrderView {

        private final long orderId;
        private final String status;
        private final Instant createdAt;
        private final List<SellerOrderItemView> items = new ArrayList<>();
        private BigDecimal subtotal = BigDecimal.ZERO;

        private SellerOrderView(
                long orderId, String status, Instant createdAt) {
            this.orderId = orderId;
            this.status = status;
            this.createdAt = createdAt;
        }

        private void addItem(SellerOrderItemView item) {
            items.add(item);
            subtotal = subtotal.add(item.getLineTotal());
        }

        public long getOrderId() {
            return orderId;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAtDisplay() {
            return DATE_FORMAT.format(createdAt);
        }

        public List<SellerOrderItemView> getItems() {
            return List.copyOf(items);
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public long getUnits() {
            return items.stream()
                    .mapToLong(SellerOrderItemView::getQuantity)
                    .sum();
        }
    }

    public static final class SellerOrderItemView {

        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal lineTotal;

        private SellerOrderItemView(
                String productName,
                int quantity,
                BigDecimal unitPrice) {

            if (quantity <= 0 || unitPrice == null) {
                throw new IllegalArgumentException(
                        "Invalid seller order item.");
            }

            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity));
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }
    }

    public static final class SellerSalesSummary {

        private final long orderCount;
        private final long unitsSold;
        private final BigDecimal salesTotal;

        private SellerSalesSummary(
                long orderCount,
                long unitsSold,
                BigDecimal salesTotal) {
            this.orderCount = orderCount;
            this.unitsSold = unitsSold;
            this.salesTotal = salesTotal;
        }

        public long getOrderCount() {
            return orderCount;
        }

        public long getUnitsSold() {
            return unitsSold;
        }

        public BigDecimal getSalesTotal() {
            return salesTotal;
        }
    }
}

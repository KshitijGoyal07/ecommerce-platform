package com.ecommerce.repository.jdbc;

import com.ecommerce.db.DatabaseConnection;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.OrderStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Transactional JDBC operations for creating, loading, and updating orders. */
public final class OrderDao {

    /**
     * Places an order in one transaction: locks products, validates and reserves stock,
     * inserts the order and its item price snapshots, then commits. Any failure rolls back
     * the stock changes and inserts together.
     */
   public long placeOrder(long buyerId, List<OrderLineRequest> requestedLines,
                       String shippingAddressSnapshot) throws SQLException {
        return placeOrder(
                buyerId,
                requestedLines,
                shippingAddressSnapshot,
                "COD"
        );
    }

    public long placeOrder(long buyerId, List<OrderLineRequest> requestedLines,
                        String shippingAddressSnapshot,
                        String paymentMethod) throws SQLException {
        if (buyerId <= 0) {
            throw new IllegalArgumentException("Buyer ID must be positive.");
        }
        if (requestedLines == null || requestedLines.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item.");
        }
        if (shippingAddressSnapshot == null || shippingAddressSnapshot.isBlank()) {
            throw new IllegalArgumentException("Shipping address snapshot must not be blank.");
        }
        String selectedPaymentMethod =
                paymentMethod == null ? "" : paymentMethod.trim();

        if (!Set.of("COD", "UPI", "CARD").contains(selectedPaymentMethod)) {
            throw new IllegalArgumentException("Select a valid payment method.");
        }

        String paymentStatus = "COD".equals(selectedPaymentMethod)
                ? "PENDING"
                : "SIMULATED";

        List<OrderLineRequest> lines = new ArrayList<>(requestedLines);
        if (lines.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Order lines cannot contain null values.");
        }
        Set<Long> productIds = new HashSet<>();
        for (OrderLineRequest line : lines) {
            if (!productIds.add(line.productId())) {
                throw new IllegalArgumentException("Each product may appear only once in an order.");
            }
        }
        // Lock in a deterministic order to reduce deadlock risk for overlapping baskets.
        lines.sort(Comparator.comparingLong(OrderLineRequest::productId));

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureActiveBuyer(connection, buyerId);
                List<SelectedLine> selectedLines = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                for (OrderLineRequest line : lines) {
                    SelectedLine selected = lockAndReserveProduct(connection, line);
                    selectedLines.add(selected);
                    total = total.add(selected.unitPrice().multiply(BigDecimal.valueOf(line.quantity())));
                }

                long orderId = insertOrder(
                        connection,
                        buyerId,
                        total,
                        shippingAddressSnapshot.trim(),
                        selectedPaymentMethod,
                        paymentStatus
                );
                insertOrderItems(connection, orderId, selectedLines);
                connection.commit();
                return orderId;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        }
    }

    /** Returns a buyer's order history newest first; callers cannot query another buyer's orders. */
    public List<PersistedOrder> findByBuyerId(long buyerId) throws SQLException {
        requirePositiveId(buyerId, "Buyer ID");
        
    String sql = "SELECT order_id, buyer_id, status, payment_method, payment_status, "
            + "total_amount, shipping_address_snapshot, created_at "
            + "FROM orders WHERE buyer_id = ? "
            + "ORDER BY created_at DESC, order_id DESC";

        List<OrderHeader> headers = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Timestamp created = resultSet.getTimestamp("created_at");
                    
                headers.add(new OrderHeader(
                        resultSet.getLong("order_id"),
                        resultSet.getLong("buyer_id"),
                        toModelStatus(resultSet.getString("status")),
                        resultSet.getString("payment_method"),
                        resultSet.getString("payment_status"),
                        resultSet.getBigDecimal("total_amount"),
                        resultSet.getString("shipping_address_snapshot"),
                        created == null ? Instant.EPOCH : created.toInstant()));

                }
            }
            
                List<PersistedOrder> orders = new ArrayList<>();

                for (OrderHeader header : headers) {
                    orders.add(new PersistedOrder(
                            header.id(),
                            header.buyerId(),
                            header.status(),
                            header.paymentMethod(),
                            header.paymentStatus(),
                            header.totalAmount(),
                            header.shippingAddressSnapshot(),
                            header.createdAt(),
                            loadItems(connection, header.id())
                    ));
                }

            return List.copyOf(orders);
        }
    }

    public Optional<PersistedOrder> findById(long orderId) throws SQLException {
        requirePositiveId(orderId, "Order ID");
        
       
    String sql = "SELECT order_id, buyer_id, status, payment_method, payment_status, "
            + "total_amount, shipping_address_snapshot, created_at "
            + "FROM orders WHERE order_id = ?";


        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                long buyerId = resultSet.getLong("buyer_id");
                OrderStatus status = toModelStatus(resultSet.getString("status"));
                String paymentMethod = resultSet.getString("payment_method");
                String paymentStatus = resultSet.getString("payment_status");
                BigDecimal total = resultSet.getBigDecimal("total_amount");
                String address = resultSet.getString("shipping_address_snapshot");
                Timestamp created = resultSet.getTimestamp("created_at");
                List<PersistedOrderItem> items = loadItems(connection, orderId);
                
                return Optional.of(new PersistedOrder(
                        orderId,
                        buyerId,
                        status,
                        paymentMethod,
                        paymentStatus,
                        total,
                        address,
                        created == null ? Instant.EPOCH : created.toInstant(),
                        items
                ));

            }
        }
    }

    /** Returns false if the order does not exist; illegal lifecycle transitions are rejected. */
    public boolean updateStatus(long orderId, OrderStatus nextStatus) throws SQLException {
        requirePositiveId(orderId, "Order ID");
        if (nextStatus == null) {
            throw new IllegalArgumentException("Next order status is required.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                OrderStatus current = selectStatusForUpdate(connection, orderId);
                if (current == null) {
                    connection.commit();
                    return false;
                }
                if (!isTransitionAllowed(current, nextStatus)) {
                    throw new IllegalArgumentException(
                            "Order status cannot change from " + current + " to " + nextStatus + ".");
                }
                String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, toDatabaseStatus(nextStatus));
                    statement.setLong(2, orderId);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        }
    }

    private static SelectedLine lockAndReserveProduct(Connection connection, OrderLineRequest line)
            throws SQLException {
        String selectSql = "SELECT name, price, stock_quantity, is_active FROM products "
                + "WHERE product_id = ? FOR UPDATE";
        String name;
        BigDecimal price;
        int stock;
        boolean active;
        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setLong(1, line.productId());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Product " + line.productId() + " does not exist.");
                }
                name = resultSet.getString("name");
                price = resultSet.getBigDecimal("price");
                stock = resultSet.getInt("stock_quantity");
                active = resultSet.getBoolean("is_active");
            }
        }
        if (!active) {
            throw new IllegalStateException("Product " + line.productId() + " is inactive.");
        }
        if (stock < line.quantity()) {
            throw new InsufficientStockException(line.quantity(), stock);
        }

        String updateSql = "UPDATE products SET stock_quantity = stock_quantity - ? "
                + "WHERE product_id = ? AND is_active = TRUE AND stock_quantity >= ?";
        try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
            statement.setInt(1, line.quantity());
            statement.setLong(2, line.productId());
            statement.setInt(3, line.quantity());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Stock changed while placing the order; please retry.");
            }
        }
        return new SelectedLine(line.productId(), name, line.quantity(), price);
    }

    private static long insertOrder(
            Connection connection,
            long buyerId,
            BigDecimal total,
            String address,
            String paymentMethod,
            String paymentStatus
    ) throws SQLException {

        String sql = "INSERT INTO orders "
                + "(buyer_id, status, payment_method, payment_status, "
                + "total_amount, shipping_address_snapshot) "
                + "VALUES (?, 'PENDING', ?, ?, ?, ?)";

        try (PreparedStatement statement =
                    connection.prepareStatement(
                            sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, buyerId);
            statement.setString(2, paymentMethod);
            statement.setString(3, paymentStatus);
            statement.setBigDecimal(4, total);
            statement.setString(5, address);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "MySQL did not return a generated order ID.");
                }

                return keys.getLong(1);
            }
        }
    }

    private static void insertOrderItems(Connection connection, long orderId, List<SelectedLine> lines)
            throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (SelectedLine line : lines) {
                statement.setLong(1, orderId);
                statement.setLong(2, line.productId());
                statement.setInt(3, line.quantity());
                statement.setBigDecimal(4, line.unitPrice());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static List<PersistedOrderItem> loadItems(Connection connection, long orderId) throws SQLException {
        String sql = "SELECT oi.product_id, p.name AS product_name, oi.quantity, oi.unit_price, oi.line_total "
                + "FROM order_items oi JOIN products p ON p.product_id = oi.product_id "
                + "WHERE oi.order_id = ? ORDER BY oi.order_item_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<PersistedOrderItem> items = new ArrayList<>();
                while (resultSet.next()) {
                    items.add(new PersistedOrderItem(resultSet.getLong("product_id"),
                            resultSet.getString("product_name"), resultSet.getInt("quantity"),
                            resultSet.getBigDecimal("unit_price"), resultSet.getBigDecimal("line_total")));
                }
                return List.copyOf(items);
            }
        }
    }

    private static void ensureActiveBuyer(Connection connection, long buyerId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE user_id = ? AND role = 'BUYER' AND is_active = TRUE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, buyerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("An active buyer account is required to place an order.");
                }
            }
        }
    }

    private static OrderStatus selectStatusForUpdate(Connection connection, long orderId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT status FROM orders WHERE order_id = ? FOR UPDATE")) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? toModelStatus(resultSet.getString("status")) : null;
            }
        }
    }

    private static OrderStatus toModelStatus(String databaseStatus) {
        return "PENDING".equals(databaseStatus) ? OrderStatus.PLACED : OrderStatus.valueOf(databaseStatus);
    }

    private static String toDatabaseStatus(OrderStatus status) {
        return status == OrderStatus.PLACED ? "PENDING" : status.name();
    }

    private static boolean isTransitionAllowed(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PLACED -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private static void requirePositiveId(long id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException(name + " must be positive.");
        }
    }

    private record SelectedLine(long productId, String productName, int quantity, BigDecimal unitPrice) {
    }

    private record OrderHeader(long id, long buyerId, OrderStatus status, String paymentMethod, String paymentStatus, BigDecimal totalAmount,
                               String shippingAddressSnapshot, Instant createdAt) {
    }
}

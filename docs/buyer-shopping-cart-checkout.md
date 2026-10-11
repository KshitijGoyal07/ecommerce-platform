# Buyer shopping cart, checkout, and order history

## Features

- A session-scoped cart with validated quantities and add/update/remove/clear actions.
- Server-side reloading of active products, current pricing, and stock before displaying cart totals.
- CSRF tokens on mutating cart and checkout forms.
- Checkout creates an order using `OrderDao.placeOrder`, which rechecks price and stock in a database transaction, locks product rows, reserves stock, stores the order and line items, and rolls back if any operation fails.
- Buyer order history is scoped to the authenticated buyer.
- Monetary values are formatted in Indian Rupees (INR); checkout is academic/demo only and does not contact a payment provider.

## Database permissions

Using MySQL as an administrator, grant the application account the permissions needed to insert orders and order items and read order history. The account already has `SELECT` and `UPDATE` on products from the seller/product phase.

```sql
GRANT SELECT, INSERT ON marketsquare.orders
TO 'marketsquare_app'@'localhost';

GRANT SELECT, INSERT ON marketsquare.order_items
TO 'marketsquare_app'@'localhost';
```

Do not grant the application `root` privileges. Order creation uses a transaction and reads `users` to verify the buyer, reads/updates `products` to validate and reserve stock, then inserts `orders` and `order_items`.

## Local verification

1. Build and run tests: `mvn clean test`.
2. Rebuild the WAR: `mvn clean package`.
3. Deploy the new `target/ecommerce-platform.war` to Tomcat with `MARKETSQUARE_DB_URL`, `MARKETSQUARE_DB_USER`, and `MARKETSQUARE_DB_PASSWORD` set in Tomcat's process.
4. Sign in as a Buyer and add an in-stock product to the cart.
5. Update quantity, remove a line, and verify subtotal formatting uses INR.
6. Continue to checkout and submit a test delivery address. No real payment is collected.
7. Verify the order appears in `/buyer/orders` and product stock decreases.

The cart lives in the web session; completed orders and stock changes are persisted in MySQL.

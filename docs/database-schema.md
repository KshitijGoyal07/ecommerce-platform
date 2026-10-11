# MarketSquare Database Schema

The project uses MySQL 8.4 and the schema script at `src/main/resources/db/schema.sql`.

## Tables

- `users`: buyer, seller, and administrator accounts; stores a password hash, never a plaintext password.
- `categories`: product categories.
- `products`: seller-owned listings, optional uploaded image filename, prices, stock quantities, and listing state.
- `orders`: buyer order header, order status, total, and an address snapshot for historical accuracy.
- `order_items`: products and quantities belonging to an order; preserves the price at purchase time.
- `user_addresses`: a user's address book.
- `payment_methods`: safe display metadata and optional provider token references; never stores full card numbers or CVVs.
- `wishlists`: buyer's saved products, with duplicate buyer/product entries prevented.
- `product_view_history`: recently viewed products.
- `system_activity_logs`: activity records for administrative monitoring.

## Apply the schema

1. Open MySQL Command Line Client and sign in.
2. Run `SOURCE C:/full/path/to/ecommerce-platform/src/main/resources/db/schema.sql;` (replace the example with the actual absolute path).
3. Verify with `USE marketsquare;` and `SHOW TABLES;`.

The script uses `CREATE DATABASE IF NOT EXISTS` and `CREATE TABLE IF NOT EXISTS`, so it can be rerun for initial setup. It does not alter existing table definitions if they already exist. For an existing installation without product photos, run `src/main/resources/db/migration/V1__product_images.sql` once before deploying the updated application.

## Design notes

- Prices use `DECIMAL` to avoid floating-point money calculations.
- Foreign keys protect referential integrity.
- Product listing state uses `is_active` so a product can be hidden without removing order history.
- `image_path` stores only a generated filename. Image files live outside the WAR in `<CATALINA_BASE>/marketsquare-uploads/products` by default. Set `MARKETSQUARE_UPLOAD_DIR` to choose another persistent directory.
- Order items capture `unit_price` at purchase time; changing a product's current price must not rewrite past orders.
- The schema is a starting point for the course project. Role-specific authorization and validation must also be enforced in Java services.

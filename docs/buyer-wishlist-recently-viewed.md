# Phase 4D — Buyer Wishlist and Recently Viewed

## Features
- Buyers can save active products to a MySQL-backed wishlist and remove saved products.
- The marketplace shows whether a product has already been saved.
- Product detail pages record each buyer's view in `product_view_history`.
- The buyer dashboard shows distinct recently viewed active products, newest view first.
- Product pages, wishlist prices, and other displayed money amounts use INR.
- Wishlist mutations use the existing session-bound CSRF token, prepared SQL statements, and buyer-role checks.

## Existing database tables
The repository schema already defines `wishlists` and `product_view_history` in `src/main/resources/db/schema.sql`. Verify they exist with:

```sql
USE marketsquare;
SHOW TABLES LIKE 'wishlists';
SHOW TABLES LIKE 'product_view_history';
```

If either is missing, run the existing schema script from MySQL Command Line Client using its full Windows path, for example:

```sql
SOURCE C:/Users/your-windows-user/OneDrive/Desktop/ecommerce-platform-phase1/ecommerce-platform/src/main/resources/db/schema.sql;
```

Replace the example path with the actual path to this project. The schema uses `CREATE TABLE IF NOT EXISTS` and does not alter existing table definitions.

## Grant the least required permissions
Run these commands as MySQL root/admin:

```sql
GRANT SELECT, INSERT, DELETE ON marketsquare.wishlists
TO 'marketsquare_app'@'localhost';

GRANT SELECT, INSERT ON marketsquare.product_view_history
TO 'marketsquare_app'@'localhost';

SHOW GRANTS FOR 'marketsquare_app'@'localhost';
```

No `UPDATE` grant is needed for `product_view_history`; each product detail visit creates a new history record.

## Routes
- `GET /buyer/dashboard` — catalogue, wishlist state, recently viewed products.
- `GET /buyer/product?id=<productId>` — product details; records a view.
- `GET /buyer/wishlist` — saved product list.
- `POST /buyer/wishlist` — add/remove, with CSRF validation.

## Build and verification
From the project root:

```powershell
mvn clean test
mvn clean package
```

Then deploy `target/ecommerce-platform.war` to Tomcat using the project's existing local setup. Test saving a product, removing it, viewing a product detail page, and verifying that it appears in the Recently viewed section of the buyer dashboard.

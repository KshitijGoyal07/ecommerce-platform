MarketSquare Phase 4D — Wishlist and Recently Viewed

Patch contents are rooted at the project root. Extract this ZIP into the existing folder that contains pom.xml and merge/replace files when prompted.

Features:
- Persistent buyer wishlist in MySQL.
- Remove products from the wishlist.
- Product detail page that records a view for the signed-in buyer.
- Buyer dashboard section for recently viewed active products.
- CSRF validation for wishlist changes.
- INR currency formatting remains in place.

IMPORTANT: Before testing, grant the app database account the following permissions in MySQL as root/admin:

GRANT SELECT, INSERT, DELETE ON marketsquare.wishlists TO 'marketsquare_app'@'localhost';
GRANT SELECT, INSERT ON marketsquare.product_view_history TO 'marketsquare_app'@'localhost';

The repository schema already defines these tables. Verify that they exist; if not, follow docs/buyer-wishlist-recently-viewed.md to apply the schema.sql file.

After extraction, in the VS Code terminal at the project root run:

mvn clean test

Then send the result before deploying. Do not replace pom.xml, DatabaseConnection.java, the order files, checkout files, or seller files.

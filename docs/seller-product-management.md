# Seller Product and Inventory Management

MarketSquare sellers can create product listings, edit listing details, set stock quantity, and deactivate listings from `/seller/dashboard`. Product writes are always scoped to the currently authenticated seller ID from the server-side session; the browser never supplies the trusted seller ID.

## Operations

- **Create:** inserts a product and finds or creates its category within a transaction.
- **Read:** the seller dashboard shows only rows owned by the signed-in seller.
- **Update details:** updates name, description, category, and price only when `product_id` and `seller_id` match.
- **Set stock:** writes an exact non-negative stock quantity, scoped to the owner.
- **Deactivate:** sets `is_active = FALSE` instead of deleting a product that could be referenced by historical orders.
- **CSRF defense:** state-changing seller forms include a random session token that is checked by the POST servlet.

## Currency

All listing prices are stored as `DECIMAL(12,2)` and represented by `BigDecimal`. The user interface labels price fields in INR and renders prices with the `en_IN` locale and the `INR` currency code (₹).

## Database permissions

Log in to MySQL as an administrator and grant the application account only the additional privileges required for seller listing operations:

```sql
GRANT SELECT, INSERT, UPDATE
ON marketsquare.products
TO 'marketsquare_app'@'localhost';

GRANT SELECT, INSERT, UPDATE
ON marketsquare.categories
TO 'marketsquare_app'@'localhost';
```

These statements upgrade the existing read-only grants on those tables. No DELETE privilege is required because listing removal is implemented as deactivation.

## Verification

Run `mvn clean test` for input validation tests. For a local end-to-end check, sign in using a **Seller** account, create a product with an INR price, confirm it appears in the seller listing, then sign in as a Buyer and confirm the active product appears in the catalogue. Test edit, set-stock, and deactivate operations, and check that a different seller cannot update another seller's product.


## Optional product photos

- Product photos are optional. Sellers can create or edit listings without uploading an image.
- Uploads accept JPEG and PNG files up to 5 MB. The app validates and re-encodes images before storage.
- The database stores only the generated image filename. Files are saved outside the WAR by default in `<CATALINA_BASE>/marketsquare-uploads/products`; set `MARKETSQUARE_UPLOAD_DIR` to use a different persistent directory.
- Fresh databases get the nullable `products.image_path` column from `src/main/resources/db/schema.sql`. Existing databases must run `src/main/resources/db/migration/V1__product_images.sql` once before deploying the updated code.

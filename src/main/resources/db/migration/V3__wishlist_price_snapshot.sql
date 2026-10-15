ALTER TABLE wishlists
    ADD COLUMN saved_price DECIMAL(12,2) NULL;

UPDATE wishlists w
JOIN products p ON p.product_id = w.product_id
SET w.saved_price = p.price
WHERE w.saved_price IS NULL;

ALTER TABLE wishlists
    MODIFY saved_price DECIMAL(12,2) NOT NULL;
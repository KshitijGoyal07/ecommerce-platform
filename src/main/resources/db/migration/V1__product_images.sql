-- Run once on an existing MarketSquare database without products.image_path.
-- Fresh databases already have this nullable column in schema.sql.
USE marketsquare;
ALTER TABLE products ADD COLUMN image_path VARCHAR(255) NULL AFTER description;

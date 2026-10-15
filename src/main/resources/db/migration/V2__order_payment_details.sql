
USE marketsquare;

-- Add payment method if it does not already exist.
SET @has_payment_method = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'payment_method'
);

SET @migration_sql = IF(
    @has_payment_method = 0,
    "ALTER TABLE orders
     ADD COLUMN payment_method
     ENUM('COD', 'UPI', 'CARD')
     NOT NULL DEFAULT 'COD'
     AFTER status",
    "SELECT 'payment_method already exists; skipping' AS migration_note"
);

PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;


-- Record the payment state.
SET @has_payment_status = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'payment_status'
);

SET @migration_sql = IF(
    @has_payment_status = 0,
    "ALTER TABLE orders
     ADD COLUMN payment_status
     ENUM('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'SIMULATED')
     NOT NULL DEFAULT 'PENDING'
     AFTER payment_method",
    "SELECT 'payment_status already exists; skipping' AS migration_note"
);

PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;


-- Store a reference for demo or future provider transactions.
SET @has_payment_reference = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'orders'
      AND column_name = 'payment_reference'
);

SET @migration_sql = IF(
    @has_payment_reference = 0,
    "ALTER TABLE orders
     ADD COLUMN payment_reference VARCHAR(100) NULL
     AFTER payment_status",
    "SELECT 'payment_reference already exists; skipping' AS migration_note"
);

PREPARE migration_stmt FROM @migration_sql;
EXECUTE migration_stmt;
DEALLOCATE PREPARE migration_stmt;

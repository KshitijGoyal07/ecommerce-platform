# JDBC Connection and Product Persistence

MarketSquare uses JDBC and MySQL Connector/J to access the `marketsquare` database created by `src/main/resources/db/schema.sql`.

## Classes

- `com.ecommerce.db.DatabaseConnection`: opens connections using environment variables; credentials are not stored in Java source or Git.
- `com.ecommerce.db.DatabaseHealthCheck`: a small command-line connection check.
- `com.ecommerce.repository.jdbc.ProductDao`: creates, reads, updates, deactivates, and manages stock for products using prepared statements.
- `Product.draft(...)`: creates an unsaved product with ID `0`. `ProductDao.create(...)` inserts it and returns a new object containing the generated database ID.

## Required environment variables

- `MARKETSQUARE_DB_USER`: a MySQL account that has access to `marketsquare`.
- `MARKETSQUARE_DB_PASSWORD`: that account's password.
- `MARKETSQUARE_DB_URL`: optional; defaults to `jdbc:mysql://localhost:3306/marketsquare?serverTimezone=UTC`.

For local verification in VS Code's PowerShell terminal, set the values in that terminal session. For the root account installed during setup:

```powershell
$env:MARKETSQUARE_DB_URL = "jdbc:mysql://localhost:3306/marketsquare?serverTimezone=UTC"
$env:MARKETSQUARE_DB_USER = "root"
$securePassword = Read-Host "Enter your MySQL root password" -AsSecureString
$env:MARKETSQUARE_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
```

The password prompt is masked. These values apply only to the current terminal session. For the actual application, create a dedicated least-privilege MySQL account rather than running the web app as `root`; that account will be configured in a later checkpoint.

## Build and test

```powershell
mvn clean test
```

## Verify the live database connection

Run this in the same terminal session in which the environment variables above were set:

```powershell
mvn compile org.codehaus.mojo:exec-maven-plugin:3.5.0:java -Dexec.mainClass=com.ecommerce.db.DatabaseHealthCheck
```

A successful check prints the connected database name and MySQL server version. It never prints the database password.

## Product persistence behavior

- SQL uses prepared statements rather than concatenating user-controlled values.
- New product IDs are obtained from MySQL generated keys.
- Category names are resolved to rows in the `categories` table before storing products.
- `updateDetails` intentionally does not overwrite stock quantity; stock has separate atomic methods.
- `reserveStock` uses a conditional `UPDATE` so two simultaneous requests cannot reserve units that are no longer available.
- `deactivate` is a soft delete (`is_active = FALSE`), preserving foreign-key references from order history.
- Connections and statements are closed with try-with-resources.

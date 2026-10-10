# User and Order Persistence

MarketSquare's user and order DAOs live in `com.ecommerce.repository.jdbc` and use `DatabaseConnection` to open JDBC connections. They use prepared statements, try-with-resources, and transaction boundaries for database operations.

## User accounts

`UserDao` supports creating, looking up, listing active accounts, and soft-deactivating accounts. `UserAccount` is a read-only view and intentionally does not expose the password hash. `UserDao.create` accepts a **pre-computed password hash**, never plaintext. Password hashing and authentication are part of the later login checkpoint; do not pass a plaintext password as the `passwordHash` argument.

## Orders

`OrderDao.placeOrder` validates the active buyer and requested lines, locks product rows in ascending product-ID order, verifies available stock, reserves stock, and inserts the order header and item price snapshots within one database transaction. If anything fails before commit, the transaction rolls back so stock and order data stay consistent.

`OrderLineRequest` represents a requested product/quantity. `PersistedOrder` and `PersistedOrderItem` are read-only results returned by `OrderDao.findById`. Order status maps Java `PLACED` to the database's `PENDING` value because the model and database use different names for that initial state.

## Commands

Run unit tests:

```powershell
mvn clean test
```

The new tests validate user-account normalization and order-line input rules without requiring access to the live database. To exercise `UserDao` and `OrderDao` end to end, use a local MySQL instance with the MarketSquare schema and environment variables configured according to `docs/jdbc-and-product-persistence.md`.

## Safety notes

- Never commit local database passwords.
- Use a restricted application account for the web app rather than the MySQL `root` account.
- Store only a password hash in `users.password_hash`.
- Orders use the price captured at purchase time, not future product prices.

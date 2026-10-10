# Phase 2A — Core domain model

This addition introduces the Java domain classes for MarketSquare:

- `DashboardAccess` interface and `User` abstract base class
- `Buyer`, `Seller`, and `Admin` inheritance with role-specific polymorphic behavior
- `Product` and stock validation
- `Order`, `OrderItem`, and `OrderStatus` with order-total calculation and status-transition rules
- Custom runtime exceptions for insufficient stock and invalid order-status changes

The classes currently model in-memory business data. They are not connected to MySQL yet; JDBC repositories are planned for a later phase.

## Build check

From the project root, run:

```powershell
mvn clean package
```

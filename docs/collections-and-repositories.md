# Collections, Generics, and the Product Catalog

## Purpose

This part of MarketSquare adds a reusable repository contract and a product catalog that can search and filter products. The repository is currently in-memory; the same `Repository<T, ID>` contract can later be implemented using JDBC and MySQL.

## Key classes

- `Repository<T, ID>`: a generic interface for saving, retrieving, listing, and deleting objects. `T` is the stored type and `ID` is its identifier type.
- `InMemoryRepository<T, ID>`: a generic implementation backed by `LinkedHashMap`, which retains insertion order and provides lookup by ID.
- `ProductCatalog`: product browsing operations such as partial-name search, category filtering, price-range filtering, and hiding inactive listings.
- `InMemoryRepositoryTest` and `ProductCatalogTest`: automated tests for the repository and catalog behavior.

## Collections and generics demonstrated

- `Map<ID, T>` stores objects by ID without needing a separate repository for every model class.
- `List<T>` returns typed collections of results.
- `Optional<T>` represents a lookup that may not find an object.
- Streams and predicates filter, sort, and search product results.
- The generic repository can store any model type as long as an ID-extraction function is provided.

## Important limitation

The in-memory repository is temporary, so its data disappears when the application restarts. It is also not thread-safe at this checkpoint. Multithreading and synchronization will be addressed separately before the repository is used by concurrent workflows. MySQL persistence will replace in-memory storage in the JDBC phase.

## Build and test

Run from the project root:

```powershell
mvn clean test
```

Then package the web application:

```powershell
mvn clean package
```

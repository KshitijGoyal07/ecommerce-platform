# Concurrency and Synchronization

## Why this matters

A marketplace can receive overlapping requests. If two buyers try to reserve the final unit at once, unsynchronized stock updates can oversell that product. Concurrent writes to a shared `LinkedHashMap` can also corrupt or lose repository state.

## How this project protects shared state

- `Product` synchronizes operations that read or mutate its changing details and inventory. Only one thread at a time can reserve or restock the same product instance.
- `InMemoryRepository` synchronizes each public operation so map reads, writes, deletes, snapshots, and counts cannot interleave unsafely.
- `InventoryService` is the business-facing API for reading stock, reserving units, and restocking.
- Automated concurrency tests release multiple tasks at once, then verify that exactly the available stock can be reserved and that concurrent increments are not lost.

## Run the tests

From the project root, run:

```powershell
mvn clean test
```

The repository remains in-memory and does not persist data after the application stops. Synchronization protects concurrent operations within this running JVM; it is not a substitute for database transactions or locking across multiple application servers. The JDBC phase will introduce database transaction controls for persistent inventory updates.

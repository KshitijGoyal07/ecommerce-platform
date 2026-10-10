package com.ecommerce.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A simple generic repository backed by a LinkedHashMap.
 *
 * <p>This in-memory implementation is useful during development. It does not
 * persist data after the application stops and is not thread-safe yet; locking
 * and concurrent access are covered in the synchronization checkpoint.</p>
 *
 * @param <T>  type of object stored
 * @param <ID> type of its identifier
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    private final Map<ID, T> entities = new LinkedHashMap<>();
    private final Function<? super T, ? extends ID> idExtractor;

    /**
     * @param idExtractor function that extracts an object's ID
     */
    public InMemoryRepository(Function<? super T, ? extends ID> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor, "ID extractor must not be null.");
    }

    @Override
    public synchronized T save(T entity) {
        Objects.requireNonNull(entity, "Entity must not be null.");
        ID id = Objects.requireNonNull(idExtractor.apply(entity), "Entity ID must not be null.");
        entities.put(id, entity);
        return entity;
    }

    @Override
    public synchronized Optional<T> findById(ID id) {
        Objects.requireNonNull(id, "ID must not be null.");
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        // List.copyOf prevents callers from changing the repository through this result.
        return List.copyOf(entities.values());
    }

    @Override
    public synchronized boolean deleteById(ID id) {
        Objects.requireNonNull(id, "ID must not be null.");
        return entities.remove(id) != null;
    }

    @Override
    public synchronized long count() {
        return entities.size();
    }
}

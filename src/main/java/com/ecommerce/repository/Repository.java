package com.ecommerce.repository;

import java.util.List;
import java.util.Optional;

/**
 * A generic contract for storing and retrieving domain objects by ID.
 * The same contract can later be implemented with JDBC and MySQL.
 *
 * @param <T>  type of object stored by the repository
 * @param <ID> type of the object's identifier
 */
public interface Repository<T, ID> {

    /** Saves an object, replacing the existing object if its ID is already present. */
    T save(T entity);

    /** Looks up one object by its ID. */
    Optional<T> findById(ID id);

    /** Returns a read-only snapshot of all stored objects. */
    List<T> findAll();

    /** Deletes the object with this ID; returns false if there was no matching object. */
    boolean deleteById(ID id);

    /** Returns the number of stored objects. */
    long count();

    /** Checks whether an object with this ID is stored. */
    default boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}

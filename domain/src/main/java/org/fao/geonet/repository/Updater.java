package org.fao.geonet.repository;

/**
 * Interface for methods that need to update the an entity in on of the repository APIs.
 *
 * @param <T> the type of entity.
 */
public interface Updater<T> {
    /**
     * Updates the input entity
     * @param entity the entity to update
     */
    void apply(T entity);
}
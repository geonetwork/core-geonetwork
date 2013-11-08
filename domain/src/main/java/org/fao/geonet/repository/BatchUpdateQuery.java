package org.fao.geonet.repository;

import org.fao.geonet.repository.statistic.PathSpec;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines what elements to update, what fields in each element to update and the new values.
 *
 * User: Jesse
 * Date: 10/4/13
 * Time: 11:38 AM
 *
 * @param <T> They type of entity this query will update
 * @see org.fao.geonet.repository.GeonetRepository#createBatchUpdateQuery(org.fao.geonet.repository.statistic.PathSpec, Object,
 *      org.springframework.data.jpa.domain.Specification)
 */
public class BatchUpdateQuery<T> {
    private final Class<T> _entityClass;
    private final EntityManager _entityManager;
    private final List<PathSpec<T, ?>> _paths = new ArrayList<PathSpec<T, ?>>();
    private final List<Object> _values = new ArrayList<Object>();
    private Specification<T> _specification;

    <V> BatchUpdateQuery(@Nonnull final Class<T> entityClass, @Nonnull final EntityManager entityManager,
                         @Nonnull final PathSpec<T, V> pathSpec, @Nullable final V newValue) {
        this._entityClass = entityClass;
        this._entityManager = entityManager;
        _paths.add(pathSpec);
        _values.add(newValue);
    }

    /**
     * Add a new attribute path and value to the update query.
     *
     * @param pathSpec the path of the attribute to update with the new value.  More paths and values can be added to the {@link
     *                 BatchUpdateQuery} object after it is created.
     * @param newValue the value to set on the attribute of all the affected entities
     * @param <V>      The type of the attribute
     * @return
     */
    public <V> BatchUpdateQuery<T> add(@Nonnull final PathSpec<T, V> pathSpec, @Nullable final V newValue) {
        _paths.add(pathSpec);
        _values.add(newValue);
        return this;
    }

    /**
     * Execute the query.
     *
     * @return the number of updated elements.
     */
    public int execute() {
        int updated = 0;
        // this is a hack because at the moment hibernate JPA 2.1 is in beta and has a
        // but where only one set can be called per execution.
        // later this should be changed so that only one query is executed.
        for (int i = 0; i < _paths.size(); i++) {
            final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
            final CriteriaUpdate<T> update = cb.createCriteriaUpdate(_entityClass);
            final Root<T> root = update.from(_entityClass);

            Path<Object> path = (Path<Object>) _paths.get(i).getPath(root);
            Object value = _values.get(i);
            update.set(path, value);
            if (_specification != null) {
                update.where(_specification.toPredicate(root, null, cb));
            }
            // when only 1 query is executed this hack is also not needed.
            updated = Math.max(updated, _entityManager.createQuery(update).executeUpdate());
        }

        _entityManager.flush();
        _entityManager.clear();

        return updated;
    }

    /**
     * Set the specification used to select the entities to update.
     *
     * @param specification a specification for controlling which entities will be affected by update.
     * @return this query object
     */
    public BatchUpdateQuery<T> setSpecification(@Nonnull final Specification<T> specification) {
        this._specification = specification;
        return this;
    }
}

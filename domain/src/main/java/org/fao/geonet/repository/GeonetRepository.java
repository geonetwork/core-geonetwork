package org.fao.geonet.repository;

import org.fao.geonet.repository.statistic.PathSpec;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Methods common to all Geonetwork repositories.
 * <p/>
 *
 * @param <T>  the entity type
 * @param <ID> the id type of the entity
 *             <p/>
 *             User: jeichar
 *             Date: 9/5/13
 *             Time: 11:30 AM
 */
@NoRepositoryBean
public interface GeonetRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    /**
     * Load an entity, modify it and save it again.
     * <p>
     * This method loads the domain object identified domain object, passes the domain object
     * to the function and then saves the domain object passed to the function.
     * </p>
     *
     * @param id      the id of the domain object to load.
     * @param updater the function that updates the domain object before saving.
     * @return the saved domain object.
     */
    @Nonnull
    T update(ID id, @Nonnull Updater<T> updater);

    /**
     * Create a {@link BatchUpdateQuery} object to allow for updating multiple objects in a single query.
     *
     * @param pathToUpdate the path of the attribute to update with the new value.  More paths and values can be added to the {@link
     *                     BatchUpdateQuery} object after it is created.
     * @param newValue     the value to set on the attribute of all the affected entities
     * @param <V>          The type of the attribute
     * @return a {@link BatchUpdateQuery} object to allow for updating multiple objects in a single query.
     */
    <V> BatchUpdateQuery<T> createBatchUpdateQuery(PathSpec<T, V> pathToUpdate, V newValue);

    /**
     * Create a {@link BatchUpdateQuery} object to allow for updating multiple objects in a single query.
     *
     * @param pathToUpdate the path of the attribute to update with the new value.  More paths and values can be added to the {@link
     *                     BatchUpdateQuery} object after it is created.
     * @param newValue     the value to set on the attribute of all the affected entities
     * @param spec         a specification for controlling which entities will be affected by update.
     * @param <V>          The type of the attribute
     * @return a {@link BatchUpdateQuery} object to allow for updating multiple objects in a single query.
     */
    <V> BatchUpdateQuery<T> createBatchUpdateQuery(PathSpec<T, V> pathToUpdate, V newValue, Specification<T> spec);


    /**
     * Load all entities and convert each to XML of the form:
     * <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     *     </pre>
     *
     * @return all entities in XML.
     */
    @Nonnull
    Element findAllAsXml();

    /**
     * Load all entities that satisfy the criteria provided and convert each to XML of the form:
     * <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     *     </pre>
     *
     * @param specification A specification of the criteria that must be satisfied for entity to be selected.
     * @return all entities in XML.
     */
    @Nonnull
    Element findAllAsXml(@Nonnull Specification<T> specification);

    /**
     * Load all entities that satisfy the criteria provided and convert each to XML of the form:
     * <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     *     </pre>
     *
     * @param specification A specification of the criteria that must be satisfied for entity to be selected.
     * @param sort          the order to sort the results by
     * @return all entities in XML.
     */
    @Nonnull
    Element findAllAsXml(@Nullable Specification<T> specification, @Nullable Sort sort);

    /**
     * Load all entities that satisfy the criteria provided and convert each to XML of the form:
     * <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     *     </pre>
     *
     * @param sort the order to sort the results by
     * @return all entities in XML.
     */
    @Nonnull
    Element findAllAsXml(@Nullable Sort sort);

    /**
     * Delete all the metadata selected by the specification.
     *
     * @param specification the specification of the metadata to delete.
     *
     * @return the number of elements deleted
     */
    int deleteAll(@Nonnull Specification<T> specification);
}

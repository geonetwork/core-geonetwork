package org.fao.geonet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
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
    @Transactional
    @Nonnull
    T update(ID id, @Nonnull Updater<T> updater);

}

package org.fao.geonet.repository;

import org.fao.geonet.domain.Metadata;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

 /**
  * Abstract super class of Geonetwork repositories that contains extra useful implementations.
  *
  * @param <T> The entity type
  * @param <ID> The entity id type
  *
  * User: jeichar
  * Date: 9/5/13
  * Time: 11:26 AM
  */
public class GeonetRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements GeonetRepository<T, ID> {

    protected EntityManager _entityManager;
    private final Class<T> _entityClass;


    protected GeonetRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this._entityManager = entityManager;
        this._entityClass = domainClass;
    }


    public T update(int id, Updater<T> updater) {
        final T entity = _entityManager.find(this._entityClass, id);

        if (entity == null) {
            throw new EntityNotFoundException("No entity found with id: "+id);
        }

        updater.apply(entity);

        _entityManager.persist(entity);

        return entity;
    }
}

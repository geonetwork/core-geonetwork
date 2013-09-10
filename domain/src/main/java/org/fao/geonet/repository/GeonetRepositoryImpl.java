package org.fao.geonet.repository;

import org.fao.geonet.domain.GeonetEntity;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.List;

/**
 * Abstract super class of Geonetwork repositories that contains extra useful implementations.
 *
 * @param <T>  The entity type
 * @param <ID> The entity id type
 *             <p/>
 *             User: jeichar
 *             Date: 9/5/13
 *             Time: 11:26 AM
 */
public class GeonetRepositoryImpl<T extends GeonetEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements GeonetRepository<T, ID> {

    protected EntityManager _entityManager;
    private final Class<T> _entityClass;


    protected GeonetRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this._entityManager = entityManager;
        this._entityClass = domainClass;
    }


    public T update(ID id, Updater<T> updater) {
        final T entity = _entityManager.find(this._entityClass, id);

        if (entity == null) {
            throw new EntityNotFoundException("No entity found with id: " + id);
        }

        updater.apply(entity);

        _entityManager.persist(entity);

        return entity;
    }

    @Nonnull
    @Override
    public Element findAllAsXml() {
        return findAllAsXml(null, null);
    }

    @Nonnull
    @Override
    public Element findAllAsXml(final Specification<T> specification) {
        return findAllAsXml(specification, null);
    }

    @Nonnull
    @Override
    public Element findAllAsXml(final Specification<T> specification, final Sort sort) {
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(_entityClass);
        Root<T> root = query.from(_entityClass);

        if (specification != null) {
            final Predicate predicate = specification.toPredicate(root, query, cb);
            query.where(predicate);
        }

        if (sort != null) {
            List<Order> orders = SortUtils.sortToJpaOrders(cb, sort, root);
            query.orderBy(orders);
        }

        return toXml(_entityManager.createQuery(query).getResultList());
    }

    private Element toXml(List<T> resultList) {
        Element root = new Element(_entityClass.getSimpleName().toLowerCase());

        for (T t : resultList) {
            root.addContent(t.asXml());
        }
        return root;
    }
}

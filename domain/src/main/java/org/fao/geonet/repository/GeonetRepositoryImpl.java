package org.fao.geonet.repository;

import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.repository.statistic.PathSpec;
import org.jdom.Element;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
public class GeonetRepositoryImpl<T extends GeonetEntity, ID extends Serializable> extends SimpleJpaRepository<T,
        ID> implements GeonetRepository<T, ID> {

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
        _entityManager.flush();

        return entity;
    }

    @Override
    public <V> BatchUpdateQuery<T> createBatchUpdateQuery(PathSpec<T, V> pathToUpdate, V newValue) {
        return new BatchUpdateQuery<T>(_entityClass, _entityManager, pathToUpdate, newValue);
    }

    @Override
    public <V> BatchUpdateQuery<T> createBatchUpdateQuery(PathSpec<T, V> pathToUpdate, V newValue, Specification<T> spec) {
        final BatchUpdateQuery<T> updateQuery = new BatchUpdateQuery<T>(_entityClass, _entityManager, pathToUpdate, newValue);
        updateQuery.setSpecification(spec);
        return updateQuery;
    }

    @Nonnull
    @Override
    public Element findAllAsXml() {
        return findAllAsXml(null, (Pageable) null);
    }

    @Nonnull
    @Override
    public Element findAllAsXml(final Specification<T> specification) {
        return findAllAsXml(specification, (Pageable) null);
    }

    @Nonnull
    @Override
    public Element findAllAsXml(@Nullable Pageable pageable) {
        return findAllAsXml(null, pageable);
    }

    @Nonnull
    @Override
    public Element findAllAsXml(@Nullable Specification<T> specification, @Nullable Pageable pageable) {
        return findAllAsXml(_entityManager, _entityClass, specification, pageable);
    }

    @Nonnull
    @Override
    public Element findAllAsXml(final Sort sort) {
        return findAllAsXml(null, sort);
    }

    @Override
    @Transactional
    public int deleteAll(@Nonnull Specification<T> specification) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaDelete<T> criteriaDelete = cb.createCriteriaDelete(_entityClass);
        final Root<T> from = criteriaDelete.from(_entityClass);
        final Predicate predicate = specification.toPredicate(from, null, cb);// TODO pass in delete when spring-JPA is updated
        criteriaDelete.where(predicate);

        final Query query = _entityManager.createQuery(criteriaDelete);
        final int deleted = query.executeUpdate();

        _entityManager.clear();
        return deleted;
    }

    @Nonnull
    @Override
    public Element findAllAsXml(final Specification<T> specification, final Sort sort) {
        PageRequest request = new PageRequest(0, Integer.MAX_VALUE, sort);
        return findAllAsXml(_entityManager, _entityClass, specification, request);
    }

    protected static <T extends GeonetEntity> Element findAllAsXml(EntityManager entityManager, Class<T> entityClass,
                                                                   Specification<T> specification, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        if (specification != null) {
            final Predicate predicate = specification.toPredicate(root, query, cb);
            query.where(predicate);
        }

        if (pageable != null) {
            if (pageable.getSort() != null) {
                List<Order> orders = SortUtils.sortToJpaOrders(cb, pageable.getSort(), root);
                query.orderBy(orders);
            }
        }

        Element rootEl = new Element(entityClass.getSimpleName().toLowerCase());

        final TypedQuery<T> typedQuery = entityManager.createQuery(query);
        if (pageable != null) {
            typedQuery.setFirstResult(pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }
        for (T t : typedQuery.getResultList()) {
            rootEl.addContent(t.asXml());
        }
        return rootEl;
    }
}

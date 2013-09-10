package org.fao.geonet.repository;

import org.fao.geonet.domain.Localized;
import org.jdom.Element;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.Transient;
import javax.persistence.criteria.*;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Support class which provides the methods for implementing the methods of LocalizedEntityRepository.
 * <p/>
 * User: Jesse
 * Date: 9/9/13
 * Time: 3:40 PM
 */
public abstract class LocalizedEntityRepositoryImpl<T extends Localized, ID extends Serializable> implements
        LocalizedEntityRepository<T, ID> {
    public static final String LABEL_EL_NAME = "label";
    public static final String RECORD_EL_NAME = "record";

    private Class<T> _entityType;

    /**
     * Constructor.
     *
     * @param entityType the concrete class of the entity
     */
    public LocalizedEntityRepositoryImpl(Class<T> entityType) {
        this._entityType = entityType;
    }

    /**
     * Get the entity manager.
     */
    protected abstract EntityManager getEntityManager();

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
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(_entityType);
        Root<T> root = query.from(_entityType);

        if (specification != null) {
            final Predicate predicate = specification.toPredicate(root, query, cb);
            query.where(predicate);
        }

        if (sort != null) {
            List<Order> orders = SortUtils.sortToJpaOrders(cb, sort, root);
            query.orderBy(orders);
        }

        return toXml(getEntityManager().createQuery(query).getResultList());
    }

    private Element toXml(List<T> resultList) {
        Element root = new Element(_entityType.getSimpleName().toLowerCase());

        for (T t : resultList) {
            root.addContent(t.asXml());
        }
        return root;
    }
}

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

    private Class<T> _entityType;

    /**
     * Constructor.
     *
     * @param entityType the concrete class of the entity
     */
    public LocalizedEntityRepositoryImpl(Class<T> entityType) {
        this._entityType = entityType;
    }

}

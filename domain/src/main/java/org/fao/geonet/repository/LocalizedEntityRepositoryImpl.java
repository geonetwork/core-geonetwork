package org.fao.geonet.repository;

import org.fao.geonet.domain.Localized;
import org.jdom.Element;
import org.springframework.beans.BeanWrapperImpl;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.HashMap;
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

    protected abstract EntityManager getEntityManager();

    @Nonnull
    @Override
    public Element findAllAsXml() {
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<T> query = cb.createQuery(_entityType);

        query.from(_entityType);


        return toXml(getEntityManager().createQuery(query).getResultList());
    }

    private Element toXml(List<T> resultList) {
        Element root = new Element(_entityType.getSimpleName().toLowerCase());

        for (T t : resultList) {
            Element record = new Element(RECORD_EL_NAME);
            root.addContent(record);
            BeanWrapperImpl wrapper = new BeanWrapperImpl(t);

            for (PropertyDescriptor desc : wrapper.getPropertyDescriptors()) {
                try {
                    if (desc.getReadMethod().getDeclaringClass() == _entityType && desc.getReadMethod().getAnnotation(Transient.class)
                                                                                   == null) {
                        final String descName = desc.getName();
                        if (descName.equalsIgnoreCase("labelTranslations")) {
                            Element labelEl = new Element(LABEL_EL_NAME);

                            Map<String, String> labels = (Map<String, String>) desc.getReadMethod().invoke(t);
                            for (Map.Entry<String, String> entry : labels.entrySet()) {
                                labelEl.addContent(new Element(entry.getKey().toLowerCase()).setText(entry.getValue()));
                            }

                            record.addContent(labelEl);
                        } else {
                            final String value;
                            value = desc.getReadMethod().invoke(t).toString();
                            record.addContent(
                                    new Element(descName.toLowerCase()).setText(value)
                            );
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return root;
    }
}

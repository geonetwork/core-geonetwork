package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataCategory_;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

/**
 * Implementation for custom category methods.
 * <p/>
 * User: Jesse
 * Date: 9/9/13
 * Time: 8:00 PM
 */
public class MetadataCategoryRepositoryImpl extends LocalizedEntityRepositoryImpl<MetadataCategory, Integer> implements
        MetadataCategoryRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;

    /**
     * Constructor.
     */
    public MetadataCategoryRepositoryImpl() {
        super(MetadataCategory.class);
    }

    @Nullable
    @Override
    public MetadataCategory findOneByNameIgnoreCase(@Nonnull String name) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<MetadataCategory> cbQuery = cb.createQuery(MetadataCategory.class);
        final Root<MetadataCategory> root = cbQuery.from(MetadataCategory.class);
        final Expression<String> lowerName = cb.lower(root.get(MetadataCategory_.name));
        final Expression<String> lowerRequiredName = cb.lower(cb.literal(name));
        cbQuery.where(cb.equal(lowerName, lowerRequiredName));
        return _entityManager.createQuery(cbQuery).getSingleResult();
    }
}

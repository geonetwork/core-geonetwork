package org.fao.geonet.repository;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataCategory_;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
public class MetadataCategoryRepositoryImpl implements MetadataCategoryRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;


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

    @Override
    @Transactional
    public void deleteCategoryAndMetadataReferences(int id) {
        /*
         * Start of HACK.
          *
          * The following select seems to be needed so that the delete from below will actually delete elements...
          * At least in the unit tests.
         */
        final Query nativeQuery2 = _entityManager.createNativeQuery("Select * from " + Metadata.METADATA_CATEG_JOIN_TABLE_NAME + " WHERE "
                                                                    + Metadata.METADATA_CATEG_JOIN_TABLE_CATEGORY_ID + "=" + id);

        nativeQuery2.setMaxResults(1);
        nativeQuery2.getResultList();
        // END HACK

        final Query nativeQuery = _entityManager.createNativeQuery("DELETE FROM " + Metadata.METADATA_CATEG_JOIN_TABLE_NAME + " WHERE "
                                                                   + Metadata.METADATA_CATEG_JOIN_TABLE_CATEGORY_ID + "=?");
        nativeQuery.setParameter(1, id);
        nativeQuery.executeUpdate();


        _entityManager.flush();
        _entityManager.clear();

        final MetadataCategory reference = _entityManager.getReference(MetadataCategory.class, id);
        _entityManager.remove(reference);


        _entityManager.flush();
        _entityManager.clear();
    }
}

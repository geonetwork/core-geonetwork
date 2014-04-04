package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId_;
import org.fao.geonet.domain.MetadataRatingByIp_;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

/**
 * Implementation for MetadataRatingByIpRepositoryCustom interface.
 * <p/>
 * User: jeichar
 * Date: 9/5/13
 * Time: 4:15 PM
 */
public class MetadataRatingByIpRepositoryImpl implements MetadataRatingByIpRepositoryCustom {

    @PersistenceContext
    private EntityManager _entityManager;

    @Override
    public int averageRating(final int metadataId) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<Double> cbQuery = cb.createQuery(Double.class);
        Root<MetadataRatingByIp> root = cbQuery.from(MetadataRatingByIp.class);

        Expression<Double> mean = cb.avg(root.get(MetadataRatingByIp_.rating));
        cbQuery.select(mean);
        cbQuery.where(cb.equal(root.get(MetadataRatingByIp_.id).get(MetadataRatingByIpId_.metadataId), metadataId));
        return _entityManager.createQuery(cbQuery).getSingleResult().intValue();
    }

    @Override
    @Transactional
    public int deleteAllById_MetadataId(final int metadataId) {
        String entityType = MetadataRatingByIp.class.getSimpleName();
        String metadataIdPropName = MetadataRatingByIpId_.metadataId.getName();
        Query query = _entityManager.createQuery("DELETE FROM " + entityType + " WHERE " + metadataIdPropName + " = " + metadataId);
        return query.executeUpdate();
    }
}

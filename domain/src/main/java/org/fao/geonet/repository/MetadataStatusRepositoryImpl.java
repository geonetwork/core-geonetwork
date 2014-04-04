package org.fao.geonet.repository;

import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId_;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataValidation} entities.
 *
 * @author Jesse
 */
public class MetadataStatusRepositoryImpl implements MetadataValidationRepositoryCustom {

    @PersistenceContext
    EntityManager _entityManager;

    @Override
    @Transactional
    public int deleteAllById_MetadataId(final int metadataId) {
        String entityType = MetadataStatus.class.getSimpleName();
        String metadataIdPropName = MetadataStatusId_.metadataId.getName();
        Query query = _entityManager.createQuery("DELETE FROM " + entityType + " WHERE " + metadataIdPropName + " = " + metadataId);
        final int deleted = query.executeUpdate();
        _entityManager.flush();
        _entityManager.clear();
        return deleted;
    }
}

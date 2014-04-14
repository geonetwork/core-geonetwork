package org.fao.geonet.repository;

import org.fao.geonet.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;


public class MetadataFileUploadRepositoryImpl implements MetadataFileUploadRepositoryCustom {
    @PersistenceContext
    EntityManager _entityManager;

    @Override
    public MetadataFileUpload findByMetadataIdAndFileNameNotDeleted(int metadataId, String fileName) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<MetadataFileUpload> cbQuery = cb.createQuery(MetadataFileUpload.class);
        final Root<MetadataFileUpload> root = cbQuery.from(MetadataFileUpload.class);

        final Expression<Integer> metadataIdPath = root.get(MetadataFileUpload_.metadataId);
        final Expression<String> fileNamePath = root.get(MetadataFileUpload_.fileName);
        final Expression<String> deletedPath = root.get(MetadataFileUpload_.deletedDate);

        cbQuery.where(cb.and(
                        cb.and(cb.equal(metadataIdPath, metadataId), cb.equal(fileNamePath, fileName))),
                        cb.isNull(deletedPath)
        );

        return _entityManager.createQuery(cbQuery).getSingleResult();
    }
}

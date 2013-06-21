package org.fao.geonet.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.OperationAllowed_;

public class OperationAllowedRepositoryImpl implements OperationAllowedRepositoryCustom {

    @PersistenceContext
    EntityManager _entityManager;
    @Override
    public List<OperationAllowed> findByMetadataId (String metadataId) {
        CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<OperationAllowed> query = builder.createQuery(OperationAllowed.class);
        
        int iMdId = Integer.parseInt(metadataId);
        Root<OperationAllowed> root = query.from(OperationAllowed.class);
        
        query.where(builder.equal(builder.literal(iMdId), root.get(OperationAllowed_.id).get(OperationAllowedId_.metadataId)));
        return _entityManager.createQuery(query).getResultList();
    }
}

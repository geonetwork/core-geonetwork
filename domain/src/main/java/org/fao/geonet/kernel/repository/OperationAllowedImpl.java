package org.fao.geonet.kernel.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.fao.geonet.kernel.domain.OperationAllowed;

public class OperationAllowedImpl implements OperationAllowedRepositoryCustom {

    @PersistenceContext
    EntityManager _entityManager;
    @Override
    public List<OperationAllowed> findByMetadataId(String metadataId) {
//
//        CriteriaBuilder builder = _entityManager.getCriteriaBuilder();
//        CriteriaQuery<OperationAllowed> query = builder.createQuery(OperationAllowed.class);
//
//        int iMdId = Integer.parseInt(metadataId);
//        _entityManager.getMetamodel().managedType(OperationAllowed.class).getAttribute(OperationAllowed.ATT_METADATA_ID);
//        query.where(builder.equal(builder.literal(iMdId), builder.))
//        return _entityManager.createNamedQuery(name, resultClass)(OperationAllowed.class, Integer.parseInt(metadataId));
        return null;
    }

}

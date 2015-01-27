package org.fao.geonet.repository;

import org.fao.geonet.domain.InspireAtomFeed;
import org.fao.geonet.domain.InspireAtomFeed_;
import org.fao.geonet.domain.Metadata;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.*;


public class InspireAtomFeedRepositoryImpl implements InspireAtomFeedRepositoryCustom {
    @PersistenceContext
    private EntityManager _entityManager;


    @Override
    public String retrieveDatasetUuidFromIdentifierNs(String datasetIdCode, String datasetIdNs) {

        String metadataUuid = "";

        /*
        "SELECT m.uuid FROM Metadata m " +
                    "LEFT JOIN inspireatomfeed f ON m.id = f.metadataId " +
                    "WHERE f.atomdatasetid = ? and f.atomdatasetns = ?"
         */
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<InspireAtomFeed> cbQuery = cb.createQuery(InspireAtomFeed.class);
        final Root<InspireAtomFeed> root = cbQuery.from(InspireAtomFeed.class);

        Path<String> datasetIdCodeAttributePath = root.get(InspireAtomFeed_.atomDatasetid);
        Path<String> datasetIdNsAttributePath = root.get(InspireAtomFeed_.atomDatasetns);

        Predicate datasetIdCodePredicate = cb.equal(datasetIdCodeAttributePath, datasetIdCode);
        Predicate datasetIdNsPredicate = cb.equal(datasetIdNsAttributePath, datasetIdNs);

        cbQuery.where(cb.and(datasetIdCodePredicate, datasetIdNsPredicate));

        InspireAtomFeed feed = null;

        try{
            feed = _entityManager.createQuery(cbQuery).getSingleResult();
        } catch (NoResultException nre) {
            //Ignore this
        }

        if (feed != null) {
            Metadata md = _entityManager.find(Metadata.class, feed.getMetadataId());
            metadataUuid = md.getUuid();
        }

        return metadataUuid;
    }

    @Override
    public String retrieveDatasetUuidFromIdentifier(final String datasetIdCode) {
        String metadataUuid = "";

        /*
        "SELECT m.uuid FROM Metadata m " +
                "LEFT JOIN inspireatomfeed f ON m.id = f.metadataId " +
                "WHERE f.atomdatasetid = ?";
         */

        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<InspireAtomFeed> cbQuery = cb.createQuery(InspireAtomFeed.class);
        final Root<InspireAtomFeed> root = cbQuery.from(InspireAtomFeed.class);

        Path<String> datasetIdCodeAttributePath = root.get(InspireAtomFeed_.atomDatasetid);

        Predicate datasetIdCodePredicate = cb.equal(datasetIdCodeAttributePath, datasetIdCode);

        cbQuery.where(datasetIdCodePredicate);

        InspireAtomFeed feed = null;
        try{
            feed = _entityManager.createQuery(cbQuery).getSingleResult();
        } catch (NoResultException nre) {
            //Ignore this
        }

        if (feed != null) {
            Metadata md = _entityManager.find(Metadata.class, feed.getMetadataId());
            metadataUuid = md.getUuid();
        }

        return metadataUuid;
    }
}

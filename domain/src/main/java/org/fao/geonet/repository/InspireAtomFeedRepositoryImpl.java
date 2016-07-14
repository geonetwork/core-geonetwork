/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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

        try {
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
        try {
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

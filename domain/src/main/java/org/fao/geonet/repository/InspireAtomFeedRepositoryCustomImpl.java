/*
 * Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import java.util.List;


public class InspireAtomFeedRepositoryCustomImpl implements InspireAtomFeedRepositoryCustom {
    @PersistenceContext
    private EntityManager _entityManager;


    @Override
    public String retrieveDatasetUuidFromIdentifierNs(String datasetIdCode, String datasetIdNs) {

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

        return executeQueryAndGetMetadataUuid(cbQuery);
    }

    @Override
    public String retrieveDatasetUuidFromIdentifier(final String datasetIdCode) {

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

        return executeQueryAndGetMetadataUuid(cbQuery);
    }

    /**
     * Execute the query and return the metadata UUID of the first result found.
     *
     * @param cbQuery the query.
     * @return a metadata UUID or the empty string if no InspireAtomFeed instances matching the query are found.
     */
    private String executeQueryAndGetMetadataUuid(CriteriaQuery<InspireAtomFeed> cbQuery) {
        String metadataUuid = "";
        List<InspireAtomFeed> feeds = _entityManager.createQuery(cbQuery).getResultList();

        if (!feeds.isEmpty()) {
            // Several feeds can point to the same dataset, use the first
            Metadata md = _entityManager.find(Metadata.class, feeds.get(0).getMetadataId());
            metadataUuid = md.getUuid();
        }

        return metadataUuid;
    }
}

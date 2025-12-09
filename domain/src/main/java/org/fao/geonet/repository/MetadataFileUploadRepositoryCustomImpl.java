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

import org.fao.geonet.domain.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

/**
 * Implementation for methods in {@link MetadataFileUploadRepositoryCustom}.
 *
 * @author Jose García
 */
public class MetadataFileUploadRepositoryCustomImpl implements MetadataFileUploadRepositoryCustom {
    @PersistenceContext
    EntityManager _entityManager;

    /**
     * Returns a {@link org.fao.geonet.domain.MetadataFileUpload} by file name that is not deleted.
     */
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

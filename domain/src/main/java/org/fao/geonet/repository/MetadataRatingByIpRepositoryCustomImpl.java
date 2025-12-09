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

import org.fao.geonet.domain.MetadataRatingByIp;
import org.fao.geonet.domain.MetadataRatingByIpId_;
import org.fao.geonet.domain.MetadataRatingByIp_;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

/**
 * Implementation for MetadataRatingByIpRepositoryCustom interface.
 * <p/>
 * User: jeichar Date: 9/5/13 Time: 4:15 PM
 */
public class MetadataRatingByIpRepositoryCustomImpl implements MetadataRatingByIpRepositoryCustom {

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
        String qlString =
            String.format("DELETE FROM %s WHERE %s = :metadataId", entityType, metadataIdPropName);
        Query query = _entityManager.createQuery(qlString);
        query.setParameter("metadataId", + metadataId);
        return query.executeUpdate();
    }
}

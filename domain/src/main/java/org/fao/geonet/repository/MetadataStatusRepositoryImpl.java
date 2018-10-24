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

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.OperationAllowed_;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.StatusValueType;
import org.fao.geonet.domain.StatusValue_;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Data Access object for accessing {@link org.fao.geonet.domain.MetadataValidation} entities.
 *
 * @author Jesse
 */
public class MetadataStatusRepositoryImpl implements MetadataStatusRepositoryCustom {

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

    @Override
    public int deleteAllById_UserId(final int userId) {
        String entityType = MetadataStatus.class.getSimpleName();
        String userIdPropName = MetadataStatusId_.userId.getName();
        Query query = _entityManager.createQuery("DELETE FROM " + entityType + " WHERE " + userIdPropName + " = " + userId);
        final int deleted = query.executeUpdate();
        return deleted;
    }

    @Nonnull
    @Override
    public List<MetadataStatus> findAllByIdAndByType(int metadataId, StatusValueType type, Sort sort) {
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<MetadataStatus> query = cb.createQuery(MetadataStatus.class);
        Root<MetadataStatus> metadataStatusRoot = query.from(MetadataStatus.class);
        Root<StatusValue> statusValueRoot = query.from(StatusValue.class);

        query.select(metadataStatusRoot);

        Predicate metadataIdEqualsPredicate = cb.equal(
            metadataStatusRoot.get(MetadataStatus_.id).get(MetadataStatusId_.metadataId),
            metadataId);

        Predicate mdIdEquals = cb.equal(
            metadataStatusRoot.get(MetadataStatus_.id).get(MetadataStatusId_.statusId),
            statusValueRoot.get(StatusValue_.id));

        Predicate statusTypePredicate = cb.equal(
            statusValueRoot.get(StatusValue_.type),
            type
        );

        query.where(mdIdEquals, metadataIdEqualsPredicate, statusTypePredicate);

        if (sort != null) {
            List<Order> orders = SortUtils.sortToJpaOrders(cb, sort, metadataStatusRoot);
            query.orderBy(orders);
        }

        return _entityManager.createQuery(query).getResultList();
    }

}

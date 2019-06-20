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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatusId_;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.domain.StatusValue;
import org.fao.geonet.domain.StatusValueType;
import org.fao.geonet.domain.StatusValue_;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Data Access object for accessing
 * {@link org.fao.geonet.domain.MetadataValidation} entities.
 *
 * @author Jesse
 */
public class MetadataStatusRepositoryImpl implements MetadataStatusRepositoryCustom {

    @PersistenceContext
    EntityManager _entityManager;

    @Nonnull
    @Override
    public List<MetadataStatus> findAllByIdAndByType(int metadataId, StatusValueType type, Sort sort) {
        CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        CriteriaQuery<MetadataStatus> query = cb.createQuery(MetadataStatus.class);
        Root<MetadataStatus> metadataStatusRoot = query.from(MetadataStatus.class);
        Root<StatusValue> statusValueRoot = query.from(StatusValue.class);

        query.select(metadataStatusRoot);

        Predicate metadataIdEqualsPredicate = cb
                .equal(metadataStatusRoot.get(MetadataStatus_.id).get(MetadataStatusId_.metadataId), metadataId);

        Predicate mdIdEquals = cb.equal(metadataStatusRoot.get(MetadataStatus_.id).get(MetadataStatusId_.statusId),
                statusValueRoot.get(StatusValue_.id));

        Predicate statusTypePredicate = cb.equal(statusValueRoot.get(StatusValue_.type), type);

        query.where(mdIdEquals, metadataIdEqualsPredicate, statusTypePredicate);

        if (sort != null) {
            List<Order> orders = SortUtils.sortToJpaOrders(cb, sort, metadataStatusRoot);
            query.orderBy(orders);
        }

        return _entityManager.createQuery(query).getResultList();
    }

    /**
     * Search status.
     *
     * TODO: add paging.
     *
     * @param types
     * @param ownerIds
     * @param authorIds
     * @param recordIds
     * @param dateFrom
     * @param dateTo
     * @param from
     * @param size
     * @return
     */
    public List<MetadataStatus> searchStatus(List<StatusValueType> types,
                                             List<Integer> ownerIds,
                                             List<Integer> authorIds,
                                             List<Integer> recordIds,
                                             String dateFrom, String dateTo,
                                             @Nullable Pageable pageable) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<MetadataStatus> cbQuery = cb.createQuery(MetadataStatus.class);
        final Root<MetadataStatus> metadataStatusRoot = cbQuery.from(MetadataStatus.class);
        final Root<StatusValue> statusValueRoot = cbQuery.from(StatusValue.class);

        final Path<Integer> statusIdInMetadataPath = metadataStatusRoot.get(MetadataStatus_.id)
                .get(MetadataStatusId_.statusId);
        final Path<ISODate> statusIdDatePath = metadataStatusRoot.get(MetadataStatus_.id)
                .get(MetadataStatusId_.changeDate);
        final Path<Integer> statusIdPath = statusValueRoot.get(StatusValue_.id);
        final Path<StatusValueType> statusTypePath = statusValueRoot.get(StatusValue_.type);

        Predicate statusIdJoin = cb.equal(statusIdInMetadataPath, statusIdPath);

        Predicate typeFilter = null;
        Predicate authorPredicate = null;
        Predicate ownerPredicate = null;
        Predicate recordPredicate = null;
        if (types != null) {
            Predicate typePredicate = statusTypePath.in(types);
            typeFilter = cb.and(statusIdJoin, typePredicate);
        }

        if (authorIds != null) {
            final Path<Integer> authorIdPath = metadataStatusRoot.get(MetadataStatus_.id).get(MetadataStatusId_.userId);
            authorPredicate = authorIdPath.in(authorIds);
        }
        if (ownerIds != null) {
            final Path<Integer> ownerIdPath = metadataStatusRoot.get(MetadataStatus_.owner);
            ownerPredicate = ownerIdPath.in(ownerIds);
        }

        if (recordIds != null) {
            final Path<Integer> recordIdPath = metadataStatusRoot.get(MetadataStatus_.id)
                    .get(MetadataStatusId_.metadataId);
            recordPredicate = recordIdPath.in(recordIds);
        }

        Predicate whereClause = cb.and(statusIdJoin);
        if (typeFilter != null) {
            whereClause.getExpressions().add(typeFilter);
        }
        if (authorPredicate != null) {
            whereClause.getExpressions().add(authorPredicate);
        }
        if (ownerPredicate != null) {
            whereClause.getExpressions().add(ownerPredicate);
        }
        if (recordPredicate != null) {
            whereClause.getExpressions().add(recordPredicate);
        }


        if (dateFrom != null) {
            whereClause.getExpressions().add(cb.greaterThanOrEqualTo(statusIdDatePath, new ISODate(dateFrom)));
        }
        if (dateTo != null) {
            whereClause.getExpressions().add(cb.lessThanOrEqualTo(statusIdDatePath, new ISODate(dateTo)));
        }

        cbQuery.select(metadataStatusRoot).where(whereClause);

        if (pageable != null && pageable.getSort() != null) {
            final Sort sort = pageable.getSort();
            List<Order> orders = SortUtils.sortToJpaOrders(cb, sort, metadataStatusRoot);
            cbQuery.orderBy(orders);
        }

        TypedQuery<MetadataStatus> query = _entityManager.createQuery(cbQuery);
        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return query.getResultList();
    }
}

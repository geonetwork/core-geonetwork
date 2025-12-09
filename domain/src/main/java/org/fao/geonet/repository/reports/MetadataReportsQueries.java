/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

package org.fao.geonet.repository.reports;

import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import java.util.*;

/**
 * Queries class for metadata reports.
 *
 * @author Jose García
 */
public class MetadataReportsQueries {
    private final EntityManager _entityManager;


    /**
     * Constructor.
     *
     * @param entityManager an entitymanager to use for performing and creating queries.
     */
    public MetadataReportsQueries(EntityManager entityManager) {
        this._entityManager = entityManager;
    }


    /**
     * Retrieves the metadata updated during a period of time. Optionally filters metadata in
     * groups.
     */
    public List<? extends AbstractMetadata> getUpdatedMetadata(ISODate dateFrom, ISODate dateTo, Set<Integer> groups) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Metadata> cbQuery = cb.createQuery(Metadata.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<User> userRoot = cbQuery.from(User.class);

        // Owner join
        final Path<Integer> userOwnerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.owner);
        final Path<Integer> groupOwnerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner);

        final Path<Integer> userIdPath = userRoot.get(User_.id);
        Predicate ownerPredicate = cb.equal(userOwnerPath, userIdPath);

        // Date filter
        final Path<ISODate> changeDate = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.changeDate);
        Predicate datePredicate = cb.and(cb.lessThanOrEqualTo(changeDate, dateTo), cb.greaterThanOrEqualTo(changeDate, dateFrom));

        // Template filter (only metadata)
        final Path<Character> template = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.type_JPAWorkaround);
        Predicate templatePredicate = cb.equal(template, Constants.YN_FALSE);

        // Groups query
        if (!groups.isEmpty()) {
            Predicate inGroups = groupOwnerPath.in(groups);

            cbQuery.select(metadataRoot)
                .where(cb.and(cb.and(ownerPredicate, datePredicate, templatePredicate), inGroups));

        } else {

            cbQuery.select(metadataRoot)
                .where(cb.and(cb.and(ownerPredicate, datePredicate, templatePredicate)));
        }


        cbQuery.orderBy(cb.asc(changeDate));

        return _entityManager.createQuery(cbQuery).getResultList();
    }

    /**
     * Retrieves created metadata in the period specified, that is not available in ALL group.
     * Optionally filters metadata in groups.
     */
    public List<? extends AbstractMetadata> getInternalMetadata(ISODate dateFrom, ISODate dateTo, Set<Integer> groups,
                                                                @Nonnull Specification<OperationAllowed> operationAllowedSpecification) {
        final CriteriaBuilder cb = _entityManager.getCriteriaBuilder();
        final CriteriaQuery<Metadata> cbQuery = cb.createQuery(Metadata.class);
        final Root<Metadata> metadataRoot = cbQuery.from(Metadata.class);
        final Root<User> userRoot = cbQuery.from(User.class);

        // Owner join
        final Path<Integer> userOwnerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.owner);
        final Path<Integer> groupOwnerPath = metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner);

        // Operations allowed subquery
        Subquery<Integer> subquery = cbQuery.subquery(Integer.class);
        final Root<OperationAllowed> opAllowedRoot = subquery.from(OperationAllowed.class);
        final Predicate opAllowedPredicate = operationAllowedSpecification.toPredicate(opAllowedRoot, cbQuery, cb);
        subquery.where(opAllowedPredicate);
        final Path<Integer> opAllowedMetadataId = opAllowedRoot.get(OperationAllowed_.id).get(OperationAllowedId_.metadataId);
        subquery.select(opAllowedMetadataId);

        // Metadata owner filter
        final Path<Integer> userIdPath = userRoot.get(User_.id);
        Predicate ownerPredicate = cb.equal(userOwnerPath, userIdPath);

        // Date filter
        final Path<ISODate> createDate = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.createDate);
        Predicate datePredicate = cb.and(cb.lessThanOrEqualTo(createDate, dateTo), cb.greaterThanOrEqualTo(createDate, dateFrom));

        // Template filter (only metadata)
        final Path<Character> template = metadataRoot.get(Metadata_.dataInfo).get(MetadataDataInfo_.type_JPAWorkaround);
        Predicate templatePredicate = cb.equal(template, Constants.YN_FALSE);

        // Groups query
        if (!groups.isEmpty()) {
            Predicate inGroups = groupOwnerPath.in(groups);

            cbQuery.select(metadataRoot)
                .where(cb.and(cb.not(metadataRoot.get(Metadata_.id).in(subquery)), cb.and(cb.and(ownerPredicate, datePredicate, templatePredicate), inGroups)));

        } else {

            cbQuery.select(metadataRoot)
                .where(cb.and(cb.not(metadataRoot.get(Metadata_.id).in(subquery)), cb.and(cb.and(ownerPredicate, datePredicate, templatePredicate))));
        }

        cbQuery.orderBy(cb.asc(createDate));

        return _entityManager.createQuery(cbQuery).getResultList();
    }
}

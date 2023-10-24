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

package org.fao.geonet.repository.specification;

import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.Link_;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataLink;
import org.fao.geonet.domain.MetadataLink_;
import org.fao.geonet.domain.MetadataSourceInfo_;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.OperationAllowed_;
import org.fao.geonet.domain.ReservedOperation;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LinkSpecs {
    private LinkSpecs() {
    }

    public static Specification<Link> filter(String urlPartToContain,
                                             Integer state,
                                             String associatedRecord,
                                             Integer[] groupPublishedIds,
                                             Integer[] groupOwnerIds,
                                             Integer[] editingGroupIds) {

        return new Specification<Link>() {
            @Override
            public Predicate toPredicate(Root<Link> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (state != null) {
                    Path<Integer> statePath = root.get(Link_.lastState);
                    predicates.add(cb.equal(statePath, state));
                }

                if (urlPartToContain != null) {
                    Path<String> urlPath = root.get(Link_.url);
                    predicates.add(
                        cb.like(urlPath,
                            cb.literal(String.format("%%%s%%", urlPartToContain))));
                }

                if (associatedRecord != null) {
                    Join<Link, MetadataLink> metadataJoin = root.join(Link_.records, JoinType.INNER);
                    predicates.add(
                        cb.like(
                            metadataJoin.get("metadataUuid"),
                            cb.literal(String.format("%%%s%%", associatedRecord))));
                }

                Join<Link, MetadataLink> metadataJoin = root.join(Link_.records, JoinType.INNER);
                Subquery<Integer> subquery = query.subquery(Integer.class);
                final Root<OperationAllowed> opAllowRoot = subquery.from(OperationAllowed.class);
                final Root<Metadata> metadataRoot = subquery.from(Metadata.class);

                boolean editinGroupQuery = editingGroupIds != null && editingGroupIds.length > 0;
                boolean groupPublishedQuery = groupPublishedIds != null && groupPublishedIds.length > 0;
                boolean groupOwnerQuery = groupOwnerIds != null && groupOwnerIds.length > 0;

                List<Predicate> subQueryPredicates = new ArrayList<>();

                if (editinGroupQuery) {
                    final Predicate groupOwnerPredicate =
                        metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner).in(editingGroupIds);
                    final Predicate metadataOperations = cb.equal(metadataRoot.get(Metadata_.id), opAllowRoot.get(OperationAllowed_.id).get(OperationAllowedId_.metadataId));
                    Predicate editableGroups = opAllowRoot.get(OperationAllowed_.id).get(OperationAllowedId_.groupId).in(Arrays.asList(editingGroupIds));
                    Predicate operationTypeEdit =
                        cb.equal(
                            opAllowRoot.get(OperationAllowed_.id).get(OperationAllowedId_.operationId),
                            cb.literal(ReservedOperation.editing.getId()));

                    subQueryPredicates.add(cb.or(
                        cb.and(metadataOperations, groupOwnerPredicate),
                        cb.and(editableGroups, operationTypeEdit)));
                }

                if (groupPublishedQuery) {
                    Predicate publishedToIndicatedGroup =
                        opAllowRoot.get(OperationAllowed_.id).get(OperationAllowedId_.groupId).in(groupPublishedIds);
                    Predicate operationTypeView = cb.equal(
                        opAllowRoot.get(OperationAllowed_.id).get(OperationAllowedId_.operationId),
                        cb.literal(ReservedOperation.view.getId()));

                    subQueryPredicates.add(cb.and(publishedToIndicatedGroup, operationTypeView));
                }

                if (groupOwnerQuery) {
                    final Predicate groupOwnerPredicate =
                        metadataRoot.get(Metadata_.sourceInfo).get(MetadataSourceInfo_.groupOwner).in(groupOwnerIds);

                    subQueryPredicates.add(groupOwnerPredicate);
                }


                if (subQueryPredicates.size() > 0) {
                    subquery.where(subQueryPredicates.toArray(new Predicate[]{}));

                    Path<Integer> opAllowedMetadataId = opAllowRoot.get(OperationAllowed_.id).get(OperationAllowedId_.metadataId);
                    subquery.select(opAllowedMetadataId);

                    predicates.add(metadataJoin.get(MetadataLink_.metadataId).in(subquery));
                }

                query.distinct(true);

                return cb.and(predicates.toArray(new Predicate[]{}));
            }
        };
    }
}

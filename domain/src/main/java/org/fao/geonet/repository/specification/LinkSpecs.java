/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import com.google.common.collect.Sets;
import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class LinkSpecs {
    private LinkSpecs() {
    }

    public static Specification<Link> filter(String urlPartToContain,
                                             Integer state,
                                             List<String> associatedRecords,
                                             Integer[] groupPublishedIds,
                                             Integer[] groupOwnerIds,
                                             Integer[] httpStatusValueFilter,
                                             boolean excludeHarvestedMetadataFilter,
                                             Integer[] editingGroupIds) {

        return (root, query, cb) -> {
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

            if (associatedRecords != null) {
                Join<Link, MetadataLink> metadataJoin = root.join(Link_.records, JoinType.INNER);
                predicates.add(metadataJoin.get("metadataUuid").in(associatedRecords));
            }

            if (excludeHarvestedMetadataFilter) {
                Join<Link, MetadataLink> metadataJoin = root.join(Link_.records, JoinType.INNER);

                Subquery<Integer> subquery = query.subquery(Integer.class);
                final Root<Metadata> metadataRoot = subquery.from(Metadata.class);
                Path<Character> isHarvestedAttributePath = metadataRoot.get(AbstractMetadata_.harvestInfo).get(MetadataHarvestInfo_.harvested_JPAWorkaround);
                Predicate equalHarvestPredicate = cb.equal(isHarvestedAttributePath, cb.literal(Constants.toYN_EnabledChar(false)));
                subquery.where(
                    equalHarvestPredicate);

                Path<Integer> metadataId = metadataRoot.get(AbstractMetadata_.id);
                subquery.select(metadataId);

                predicates.add(metadataJoin.get(MetadataLink_.metadataId).in(subquery));
                query.distinct(true);
            }

            if (httpStatusValueFilter != null && httpStatusValueFilter.length > 0) {
                Join<Link, LinkStatus> linkLinkStatusJoin = root.join(Link_.linkStatus, JoinType.LEFT);

                Integer[] valuesIn = Arrays.stream(httpStatusValueFilter).filter(i -> i >= 0).toArray(Integer[]::new);
                Set<Integer> setValuesNotIn = Sets.newHashSet(httpStatusValueFilter);
                setValuesNotIn.removeAll(Arrays.asList(valuesIn));
                Integer[] valuesNotIn = setValuesNotIn.stream().map(i -> -1 * i).toArray(Integer[]::new);

                if (valuesIn.length > 0) {
                    predicates.add(cb.and(
                        cb.equal(linkLinkStatusJoin.get(LinkStatus_.checkDate), root.get(Link_.lastCheck)),
                        linkLinkStatusJoin.get((LinkStatus_.statusValue)).in(Arrays.asList(
                            Arrays.stream(valuesIn).map(String::valueOf).toArray()))));
                }

                if (valuesNotIn.length > 0) {
                    predicates.add(cb.and(
                        cb.equal(linkLinkStatusJoin.get(LinkStatus_.checkDate), root.get(Link_.lastCheck)),
                        cb.not(linkLinkStatusJoin.get((LinkStatus_.statusValue)).in(Arrays.asList(
                            Arrays.stream(valuesNotIn).map(String::valueOf).toArray())))));
                }
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
        };
    }
}

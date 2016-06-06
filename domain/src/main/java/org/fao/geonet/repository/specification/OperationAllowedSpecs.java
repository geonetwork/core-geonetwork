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

import org.fao.geonet.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

import javax.persistence.criteria.*;

public final class OperationAllowedSpecs {
    private OperationAllowedSpecs() {
        // disallow instantiation
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct
     * metadataId.
     *
     * @param metadataId the id to match
     */
    public static Specification<OperationAllowed> hasMetadataId(final String metadataId) {
        return hasMetadataId(Integer.valueOf(metadataId));
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct
     * metadataId.
     *
     * @param metadataId the id to match
     */
    public static Specification<OperationAllowed> hasMetadataId(final int metadataId) {
        return new Specification<OperationAllowed>() {

            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> mdIdAttributePath = root.get(OperationAllowed_.id).get(OperationAllowedId_.metadataId);
                Predicate mdIdEqualPredicate = cb.equal(mdIdAttributePath, cb.literal(metadataId));
                return mdIdEqualPredicate;
            }
        };
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct groupId.
     *
     * @param groupId the id to match
     */
    public static Specification<OperationAllowed> hasGroupId(final int groupId) {
        return new Specification<OperationAllowed>() {

            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> mdIdAttributePath = root.get(OperationAllowed_.id).get(OperationAllowedId_.groupId);
                return cb.equal(mdIdAttributePath, cb.literal(groupId));
            }
        };
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct
     * operationId.
     *
     * @param operation the operation to match
     */
    public static Specification<OperationAllowed> hasOperation(final ReservedOperation operation) {
        return hasOperationId(operation.getId());
    }

    /**
     * A specification that is limits results to opAllowed objects that have the correct
     * operationId.
     *
     * @param operationId the id to match
     */
    public static Specification<OperationAllowed> hasOperationId(final int operationId) {
        return new Specification<OperationAllowed>() {

            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> opIdAttributePath = root.get(OperationAllowed_.id).get(OperationAllowedId_.operationId);
                Predicate opIdEqualPredicate = cb.equal(opIdAttributePath, cb.literal(operationId));
                return opIdEqualPredicate;
            }
        };
    }

    /**
     * A specification that specifies that the all group has the provided operation.
     *
     * @param operation the operation. view is probably the most commonly needed one.
     * @return A specification that specifies that the all group has the provided operation.
     */
    public static Specification<OperationAllowed> isPublic(final ReservedOperation operation) {
        return new Specification<OperationAllowed>() {

            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path<Integer> opIdAttributePath = root.get(OperationAllowed_.id).get(OperationAllowedId_.operationId);
                Path<Integer> groupIdAttributePath = root.get(OperationAllowed_.id).get(OperationAllowedId_.groupId);
                Predicate opIdEqualPredicate = cb.equal(opIdAttributePath, cb.literal(operation.getId()));
                Predicate allGroup = cb.equal(groupIdAttributePath, cb.literal(ReservedGroup.all.getId()));
                return cb.and(opIdEqualPredicate, allGroup);
            }
        };
    }

    /**
     * A specification that selects all the operations allowed for all the metadata.
     *
     * @param metadataIds the ids of all the metadata
     */
    public static Specification<OperationAllowed> hasMetadataIdIn(final Collection<Integer> metadataIds) {
        return new Specification<OperationAllowed>() {
            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(OperationAllowed_.id).get(OperationAllowedId_.metadataId).in(metadataIds);
            }
        };
    }

    /**
     * A specification that selects all the operations allowed for all the groups provided.
     *
     * @param groupIds the ids of all the groups
     */
    public static Specification<OperationAllowed> hasGroupIdIn(final Collection<Integer> groupIds) {
        return new Specification<OperationAllowed>() {
            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(OperationAllowed_.id).get(OperationAllowedId_.groupId).in(groupIds);
            }
        };
    }

    /**
     * A specification that selects all the operations allowed for all the operation ids provided.
     *
     * @param operationIds the ids of all the operations
     */
    public static Specification<OperationAllowed> hasOperationIdIn(final Collection<Integer> operationIds) {
        return new Specification<OperationAllowed>() {
            @Override
            public Predicate toPredicate(Root<OperationAllowed> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                return root.get(OperationAllowed_.id).get(OperationAllowedId_.operationId).in(operationIds);
            }
        };
    }

}

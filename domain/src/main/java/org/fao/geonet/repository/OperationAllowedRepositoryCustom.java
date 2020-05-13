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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.persistence.metamodel.SingularAttribute;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

/**
 * Custom (Non spring-data) Query methods for {@link OperationAllowed} entities.
 *
 * @author Jesse
 */
public interface OperationAllowedRepositoryCustom {
    /**
     * Converts metadataId to integer and performs a search.
     *
     * @param metadataId id of metadata
     * @return the OperationsAllowed entities with the given metadataId
     */
    @Nonnull
    List<OperationAllowed> findByMetadataId(@Nonnull String metadataId);

    /**
     * Find all the metadata owned by the user with the given userId and that satisfy the given
     * specification.
     *
     * @param userId        the id of the owning user
     * @param specification an optional specification further restricting the OperationAllowed to
     *                      load.
     * @return all the metadata owned by the user with the given userId and that satisfy the given
     * specification.
     */
    @Nonnull
    List<OperationAllowed> findAllWithOwner(@Nonnull int userId, @Nonnull Optional<Specification<OperationAllowed>> specification);

    /**
     * Find all the ids identified by the idAttribute of the values returned by the spec.
     *
     * @param spec        the specification for selecting which elements to load
     * @param idAttribute the attribute of the OperationAllowedId to return in the list
     * @return the list of ids returned.
     */
    @Nonnull
    List<Integer> findAllIds(@Nonnull Specification<OperationAllowed> spec, @Nonnull SingularAttribute<OperationAllowedId,
        Integer> idAttribute);

    /**
     * Delete all OperationsAllowed entities with the give metadata and group ids.
     *
     * @param metadataId the metadata id
     * @param groupId    the group id
     */
    @Nonnegative
    int deleteAllByMetadataIdExceptGroupId(int metadataId, int[] groupId);
}

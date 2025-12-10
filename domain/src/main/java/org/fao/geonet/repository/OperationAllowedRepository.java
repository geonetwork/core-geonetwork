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

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data Access object for finding and saving {@link OperationAllowed} entities.
 *
 * @author Jesse
 */
public interface OperationAllowedRepository extends GeonetRepository<OperationAllowed, OperationAllowedId>,
    OperationAllowedRepositoryCustom,
    JpaSpecificationExecutor<OperationAllowed> {

    /**
     * Find all operations allowed entities with the given metadataid.
     *
     * @param metadataId the metadata id
     * @return all operation allowed entities with the given metadataid.
     */
    @Nonnull
    List<OperationAllowed> findAllById_MetadataId(int metadataId);

    /**
     * Find all operations allowed entities with the given groupid.
     *
     * @param groupId the group id
     * @return all operation allowed entities with the given groupid.
     */
    @Nonnull
    List<OperationAllowed> findAllById_GroupId(int groupId);

    /**
     * Find all operations allowed entities with the given operationId.
     *
     * @param operationId the operationId
     * @return all operations allowed entities with the given operationId.
     */
    @Nonnull
    List<OperationAllowed> findAllById_OperationId(int operationId);

    /**
     * Find the one OperationAllowed entity by the operation, metadata and group ids (or null if not
     * found).
     *
     * @param groupId     the groupid
     * @param metadataId  the metadataid
     * @param operationId the operationid
     * @return the one OperationAllowed entity by the operation, metadata and group ids (or null if
     * not found).
     */
    @Nullable
    OperationAllowed findOneById_GroupIdAndId_MetadataIdAndId_OperationId(int groupId, int metadataId, int operationId);


    /**
     * Delete all the {@link OperationAllowed} with the given id in the id component selected by the
     * metadata.
     *
     * @param id          metadataid
     * @return the number of entities deleted.
     */
    @Transactional
    @Modifying(clearAutomatically=true)
    @Query("DELETE FROM OperationAllowed where id.metadataId = ?1")
    public int deleteAllByMetadataId(int id);

    /**
     * Delete all the {@link OperationAllowed} with the given id in the id component selected by the
     * operation.
     *
     * @param id          operation
     * @return the number of entities deleted.
     */
    @Transactional
    @Modifying(clearAutomatically=true)
    @Query("DELETE FROM OperationAllowed where id.operationId = ?1")
    public int deleteAllByOperationId(int id);

    /**
     * Delete all the {@link OperationAllowed} with the given id in the id component selected by the
     * group.
     *
     * @param id          group
     * @return the number of entities deleted.
     */
    @Transactional
    @Modifying(clearAutomatically=true)
    @Query("DELETE FROM OperationAllowed where id.groupId = ?1")
    public int deleteAllByGroupId(int id);

    /**
     * Delete all OperationsAllowed entities with the give metadata and not group ids.
     *
     * @param metadataId the metadata id
     * @param groupIds    the group id
     */
    @PositiveOrZero
    @Transactional
    @Modifying(clearAutomatically=true)
    @Query("DELETE FROM OperationAllowed where metadataId = :metadataId and groupId not in :groupIds")
    public int deleteAllByMetadataIdExceptGroupId(@Param("metadataId") int metadataId, @Param("groupIds") List<Integer> groupIds);


}

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

import jakarta.annotation.Nonnull;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data Access object for accessing {@link MetadataStatus} entities.
 *
 * @author Jesse
 */
public interface MetadataStatusRepository extends GeonetRepository<MetadataStatus, Integer>,
	MetadataStatusRepositoryCustom,
	JpaSpecificationExecutor<MetadataStatus> {
    /**
     * Find all the MetadataStatus objects by the associated metadata id.
     *
     * @param metadataId the metadata id.
     * @param sort       how to sort the results
     * @return all the MetadataStatus objects by the associated metadata id.
     */
    @Nonnull
    List<MetadataStatus> findAllByMetadataId(int metadataId, Sort sort);

    /**
     * Find the MetadataStatus objects by the associated metadata id, status id, user id and change date.
     *
     * @param metadataId the metadata id.
     * @param statusId status id
     * @param userId user id
     * @param changeDate change date
     * @return all the MetadataStatus objects by the associated metadata id.
     */
    @Nonnull
    MetadataStatus findOneByMetadataIdAndStatusValue_IdAndUserIdAndChangeDate(int metadataId, int statusId, int userId, ISODate changeDate);

    /**
     * Find the MetadataStatus objects by the associated metadata id, status id, user id and change date.
     *
     * @param uuid the metadata uuid id.
     * @param statusId status id
     * @param userId user id
     * @param changeDate change date
     * @return all the MetadataStatus objects by the associated metadata id.
     */
    @Nonnull
    MetadataStatus findOneByUuidAndStatusValue_IdAndUserIdAndChangeDate(String uuid, int statusId, int userId, ISODate changeDate);

    /**
     * Find the MetadataStatus objects by the associated status id, user id and change date.
     *
     * @param statusId status id
     * @param userId user id
     * @param changeDate change date
     * @return all the MetadataStatus objects by the associated metadata id.
     */
    @Nonnull
    MetadataStatus findOneByStatusValue_IdAndUserIdAndChangeDate(int statusId, int userId, ISODate changeDate);

    /**
     * Delete all the entities that are related to the indicated metadata.
     *
     * @param metadataId the id of the metadata.
     * @return the number of rows deleted.
     */

    @Modifying(clearAutomatically=true)
    @Transactional
    @Query(value="DELETE FROM MetadataStatus s WHERE s.id.metadataId = ?1")
    int deleteAllById_MetadataId(Integer metadataId);
    
    /**
     * Delete all the entities that are related to the indicated user.
     *
     * @param userId the id of the user.
     * @return the number of rows deleted.
     */

    @Modifying(clearAutomatically=true)
    @Transactional
    @Query(value="DELETE FROM MetadataStatus s WHERE s.id.userId = ?1")
    int deleteAllByIdUserId(Integer userId);

}

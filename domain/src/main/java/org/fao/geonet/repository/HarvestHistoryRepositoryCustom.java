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

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nonnull;
import java.util.Collection;

/**
 * Custom methods for interacting with HarvestHistory repository.
 * <p/>
 * User: Jesse Date: 9/21/13 Time: 11:21 AM
 */
public interface HarvestHistoryRepositoryCustom {

    /**
     * Delete all Harvest history instances whose id is in the collection of ids.
     *
     * @param ids the ids of the history elements to delete
     * @return number or entities deleted
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM HarvestHistory h where h.id in (?1)")
    int deleteAllById(Collection<Integer> ids);

    /**
     * Set the deleted flag to true in all history entities for the given uuid.
     *
     * @param harvesterUuid the harvester uuid.
     * @return number or entities modified
     */
    @Transactional
    @Query(value = "UPDATE HarvestHistory SET deleted_JpaWorkaround = 'y'" + "WHERE harvesterUuid in (:uuid)")
    @Modifying(clearAutomatically = true)
    int markAllAsDeleted(@Param("uuid") @Nonnull String harvesterUuid);
}

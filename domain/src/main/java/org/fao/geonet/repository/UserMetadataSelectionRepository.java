/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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

import org.fao.geonet.domain.UserMetadataSelection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserMetadataSelectionRepository extends GeonetRepository<UserMetadataSelection, Integer>   {

    @Query("SELECT s.metadataUuid FROM UserMetadataSelection s WHERE s.selection_id = :parentId ORDER BY s.metadataUuid")
    List<String> queryByParent(@Param("parentId") int parentId, Pageable pageable);

    @Query("SELECT s.metadataUuid FROM UserMetadataSelection s WHERE s.selection_id = :parentId ORDER BY s.metadataUuid")
    List<String> queryByParent(@Param("parentId") int parentId);
}

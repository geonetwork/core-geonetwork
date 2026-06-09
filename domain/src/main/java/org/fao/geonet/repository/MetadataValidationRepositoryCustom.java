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
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom repository methods for the MetadataValidationRepository User: Jesse Date: 9/5/13 Time:
 * 10:17 PM
 */
public interface MetadataValidationRepositoryCustom {
    /**
     * Delete all the entities that are related to the indicated metadata.
     *
     * @param metadataId the id of the metadata.
     * @return the number of rows deleted
     */
    @Modifying(clearAutomatically=true)
    @Transactional
    @Query(value="DELETE FROM MetadataValidation v where v.id.metadataId = ?1")
    int deleteAllById_MetadataId(Integer metadataId);

    /**
     * Delete all the entities that are related to the indicated metadata
     * and are internal validation (eg. XSD or schematron).
     * It will preserve INSPIRE validation results.
     *
     * @param metadataId the id of the metadata.
     * @return the number of rows deleted
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value="DELETE FROM MetadataValidation v where v.id.metadataId = ?1 AND valtype != 'inspire'")
    int deleteAllInternalValidationById_MetadataId(Integer metadataId);
}

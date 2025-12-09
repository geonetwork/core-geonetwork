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
import jakarta.annotation.Nullable;

import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.StatusValueType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Custom repository methods for the MetadataValidationRepository User: Jesse
 * Date: 9/5/13 Time: 10:17 PM
 */
public interface MetadataStatusRepositoryCustom {
    /**
     * Find all the MetadataStatus objects corresponding to a type by the associated
     * metadata id.
     *
     * @param metadataId the metadata id.
     * @param type       the status type.
     * @param sort       how to sort the results
     * @return all the MetadataStatus objects by the associated metadata id.
     */
    @Nonnull
    List<MetadataStatus> findAllByMetadataIdAndByType(int metadataId, StatusValueType type, Sort sort);

    /**
     * Find all the MetadataStatus objects corresponding to a search
     */
    @Nonnull
    List<MetadataStatus> searchStatus(List<Integer> ids,
                                      List<String> uuids,
                                      List<StatusValueType> types,
                                      List<Integer> authorIds, List<Integer> ownerIds,
                                      List<Integer> recordIds,
                                      List<String> statusIds,
                                      String dateFrom, String dateTo,
                                      @Nullable Pageable pageable);
}

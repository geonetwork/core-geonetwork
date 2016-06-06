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

/**
 * Custom crafted methods for the MetadataRatingByIpRepository.
 * <p/>
 * User: jeichar Date: 9/5/13 Time: 4:10 PM To change this template use File | Settings | File
 * Templates.
 */
public interface MetadataRatingByIpRepositoryCustom {
    /**
     * Calculate the average of all the ratings for the given metadata. <p> The method will take the
     * sum of all ratings for the metadata and divide by the number of records (the average value)
     * </p>
     *
     * @param metadataId the metadata id.
     * @return the sum of all the rating.
     */
    int averageRating(int metadataId);

    /**
     * Delete all the entities that are related to the indicated metadata.
     *
     * @param metadataId the id of the metadata.
     * @return the number of rows deleted
     */
    int deleteAllById_MetadataId(int metadataId);
}

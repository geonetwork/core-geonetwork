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
 * Repository class for InspireAtomFeed.
 *
 * @author Jose García
 */
public interface InspireAtomFeedRepositoryCustom  {
    /**
     * Retrieve metadata dataset uuid from the dataset id and dataset ns.
     *
     * @param datasetIdCode
     * @param datasetIdNs
     * @return
     */
    public String retrieveDatasetUuidFromIdentifierNs(final String datasetIdCode, final String datasetIdNs);

    /**
     * Retrieve metadata dataset uuid from the dataset id.
     *
     * @param datasetIdCode
     * @return
     */
    public String retrieveDatasetUuidFromIdentifier(final String datasetIdCode);
}

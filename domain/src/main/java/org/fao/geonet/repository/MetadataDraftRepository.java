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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.MetadataDraft;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for the {@link MetadataDraft} entities.
 * 
 * The use of this class is discouraged, you should use IMetadataUtils or IMetadataManager instead.
 *
 * @author Jesse
 */
public interface MetadataDraftRepository
        extends GeonetRepository<MetadataDraft, Integer>, 
        MetadataRepositoryCustom<MetadataDraft>, 
        JpaSpecificationExecutor<MetadataDraft> {
    /**
     * Find one metadata by the metadata's uuid.
     *
     * @param uuid the uuid of the metadata to find
     * @return one metadata or null.
     */
    @Nullable
    MetadataDraft findOneByUuid(@Nonnull String uuid);

    /**
     * Find all metadata harvested by the identified harvester.
     *
     * @param uuid the uuid of the harvester
     * @return all metadata harvested by the identified harvester.
     */
    @Nonnull
    List<MetadataDraft> findAllByHarvestInfo_Uuid(@Nonnull String uuid);
}

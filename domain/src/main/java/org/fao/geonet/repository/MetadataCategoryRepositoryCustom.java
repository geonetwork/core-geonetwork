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

import org.fao.geonet.domain.MetadataCategory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom methods for finding and saving MetadataCategory entities.
 * <p/>
 * User: Jesse Date: 9/10/13 Time: 7:23 AM
 */
public interface MetadataCategoryRepositoryCustom {
    /**
     * Find the metadata category with the given name ignoring the case of the name.
     *
     * @param name the name to use as the key.
     * @return a metadata category or null
     */
    @Nullable
    MetadataCategory findOneByNameIgnoreCase(@Nonnull String name);

    /**
     * Remove category from all metadata that references it and delete the category from the table.
     *
     * @param id id of category.
     */
    void deleteCategoryAndMetadataReferences(int id);
}

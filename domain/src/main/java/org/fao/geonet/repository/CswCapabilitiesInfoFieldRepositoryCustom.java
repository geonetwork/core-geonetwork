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

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

/**
 * Non spring-data-jpa query methods required for accessing CswServerInfo objects. User: Jesse Date:
 * 9/20/13 Time: 9:24 AM
 */
public interface CswCapabilitiesInfoFieldRepositoryCustom {
    /**
     * Load all fields in the given language and construct a CswCapabilitiesInfo object from the
     * fields.
     *
     * @param languageCode the language of the info object to load.
     * @return all fields in the given language and construct a CswCapabilitiesInfo object from the
     * fields.
     */
    @Nonnull
    CswCapabilitiesInfo findCswCapabilitiesInfo(@Nonnull String languageCode);

    /**
     * Save the info object as individual fields.
     *
     * @param info the info object to persist.
     */
    @Transactional
    void save(@Nonnull CswCapabilitiesInfo info);
}

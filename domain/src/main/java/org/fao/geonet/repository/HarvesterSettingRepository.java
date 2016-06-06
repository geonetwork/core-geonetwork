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

import org.fao.geonet.domain.HarvesterSetting;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Data Access object for accessing {@link HarvesterSetting} entities.
 *
 * @author Jesse
 */
public interface HarvesterSettingRepository extends GeonetRepository<HarvesterSetting, Integer>,
    JpaSpecificationExecutor<HarvesterSetting>, HarvesterSettingRepositoryCustom {

    /**
     * The prefix in a path for finding a setting by its id.
     */
    String ID_PREFIX = "id:";
    /**
     * The path separator.
     */
    String SEPARATOR = "/";

    /**
     * Find all the settings with the given name.
     *
     * @param name the setting name.
     * @return All settings with the given name.
     */
    List<HarvesterSetting> findAllByName(@Nonnull String name);

    /**
     * Find all the settings with the given name and value.
     *
     * @param name  the setting name.
     * @param value the setting value.
     * @return All settings with the given name and value.
     */
    List<HarvesterSetting> findAllByNameAndValue(@Nonnull String name, @Nonnull String value);

    /**
     * Find the settings with the given name and value. Null is returned if not found.
     *
     * @param name  the setting name.
     * @param value the setting value.
     * @return The setting with the given name and value.
     */
    @Nullable
    HarvesterSetting findOneByNameAndValue(@Nonnull String name, @Nonnull String value);
}

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

import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * Custom (Non spring-data) Query methods for {@link HarvesterSetting} entities.
 *
 * @author Jesse
 */
public interface HarvesterSettingRepositoryCustom {
    /**
     * Get the root settings objects (all settings without parents)
     */
    @Nonnull
    List<HarvesterSetting> findRoots();

    /**
     * Find all settings with the parent id
     *
     * @param parentid an id of the parent setting
     * @return all settings with the parent id
     */
    @Nonnull
    List<HarvesterSetting> findAllChildren(int parentid);

    /**
     * Find the ids of all the all settings with the parent id
     *
     * @param parentid an id of the parent setting
     * @return the ids of all the all settings with the parent id
     */
    @Nonnull
    List<Integer> findAllChildIds(int parentid);

    /**
     * Find all settings with the parent id and name.
     *
     * @param parentid the parentid parameter
     * @param name     the name parameter
     */
    List<HarvesterSetting> findChildrenByName(int parentid, String name);

    /**
     * Find all settings on the given path.
     *
     * @param pathToSetting the path to search for.
     * @return all settings on the given path.
     */
    List<HarvesterSetting> findAllByPath(String pathToSetting);

    /**
     * Get the first setting with the given path.
     *
     * @param pathToSetting the path.
     * @return the first setting with the given path.
     */
    HarvesterSetting findOneByPath(String pathToSetting);

    /**
     * Get all settings on given names of settings with one request
     */
    List<HarvesterSetting> findAllByNames(List<String> names);
}

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

import org.fao.geonet.domain.Group;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Data Access object for the {@link Group} entities.
 *
 * @author Jesse
 */
public interface GroupRepository extends GeonetRepository<Group, Integer>, GroupRepositoryCustom, JpaSpecificationExecutor<Group> {
    /**
     * Look up a group by its name
     *
     * @param name the name of the group
     */
    @Nullable
    Group findByName(@Nonnull String name);

    /**
     * Look up a group by its email address
     *
     * @param email the email address of the group
     */
    @Nullable
    Group findByEmail(@Nonnull String email);

    /**
     * Find all groups with a minimumProfileForPrivileges not equal to null.
     * These groups are "restricted".
     *
     * @return a list of groups with a minimumProfileForPrivileges not equal to null
     */
    @Nullable
    List<Group> findByMinimumProfileForPrivilegesNotNull();

    public
    @Nullable
    List<Group> findByLogo(@Nonnull String logo);
}

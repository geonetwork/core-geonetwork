/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Data Access object for accessing {@link User} entities.
 *
 * @author Jesse
 */
public interface UserRepository extends GeonetRepository<User, Integer>, JpaSpecificationExecutor<User>, UserRepositoryCustom {
    /**
     * Find the user identified by  the username.
     *
     * @param username the username to use in the query.
     * @return the user identified by  the username.
     */
    public User findOneByUsername(String username);

    /**
     * Find all users identified by the provided username ignoring the case.
     *
     * Old versions allowed to create users with the same username with different case.
     * New versions do not allow this.
     *
     * @param username the username.
     * @return all users with username equals ignore case the provided username.
     */
    public List<User> findByUsernameIgnoreCase(String username);

    /**
     * find all users with the given profile.
     *
     * @param profile the profile to use in search query.
     * @return all users with the given profile.
     */
    public List<User> findAllByProfile(Profile profile);
}

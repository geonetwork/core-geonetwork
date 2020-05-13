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
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserSearch;
import org.fao.geonet.domain.UserSearchFeaturedType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 *  Data Access object for the {@link UserSearch} entities.
 */
public interface UserSearchRepository extends GeonetRepository<UserSearch, Integer>, JpaSpecificationExecutor<UserSearch> {

    List<UserSearch> findAllByCreator(User creator);

    @Query("SELECT DISTINCT b FROM UserSearch b LEFT JOIN b.groups grp WHERE grp in :groups OR b.creator = :creator")
    List<UserSearch> findAllByGroupsInOrCreator(@Param("groups") Set<Group> groups, @Param("creator") User creator);

    List<UserSearch> findAllByFeaturedType(UserSearchFeaturedType featuredType);

    List<UserSearch> findAllByFeaturedType(UserSearchFeaturedType featuredType, Specification<UserSearch> spec, Pageable pageable);

    long countByFeaturedType(UserSearchFeaturedType featuredType);
}

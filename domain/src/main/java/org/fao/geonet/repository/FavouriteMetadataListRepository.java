/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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

import org.fao.geonet.domain.FavouriteMetadataList;
import org.fao.geonet.domain.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * repository for FavouriteMetadataList
 */
public interface FavouriteMetadataListRepository extends GeonetRepository<FavouriteMetadataList, Integer>   {

    List<FavouriteMetadataList> findByUser(User user);
    List<FavouriteMetadataList> findBySessionId(String sessionId);
    List<FavouriteMetadataList> findByUserOrSessionId(User user, String sessionId);

    /**
     * all publicly available lists
     */
    @Query("select umsl from FavouriteMetadataList umsl where umsl.isPublic = true")
    List<FavouriteMetadataList> findPublic();

    /**
     *  search by name (that is owned by a particular user or session)
     */
    @Query("select umsl from FavouriteMetadataList umsl where umsl.name = ?1 and (umsl.user = ?2 or umsl.sessionId = ?3)")
    FavouriteMetadataList findByNameAndUserOrSessionId(String name, User user, String sessionId);

    /**
     *  find by name (must be owned by the user)
     */
    @Query("select umsl from FavouriteMetadataList umsl where umsl.name = ?1 and umsl.user = ?2")
    FavouriteMetadataList findByNameAndUser(String name, User user);

    /**
     *  find by name (must be owned by the sessionid)
     */
    @Query("select umsl from FavouriteMetadataList umsl where umsl.name = ?1 and  umsl.sessionId = ?2")
    FavouriteMetadataList findByNameAndSessionId(String name, String sessionId);

    /**
     *  find all that are public, owned by the user, or owned by the session
     */
    @Query("select umsl from FavouriteMetadataList umsl where umsl.user = ?1 or umsl.sessionId = ?2 or umsl.isPublic = true")
    List<FavouriteMetadataList> findByUserOrSessionOrPublic(User user, String sessionId);

    /**
     *   find all that are public or owned by the user
     */
    @Query("select umsl from FavouriteMetadataList umsl where umsl.user = ?1  or umsl.isPublic = true")
    List<FavouriteMetadataList> findByUserOrPublic(User user);

    /**
     *   find all that are public or owned by the session
     */
    @Query("select umsl from FavouriteMetadataList umsl where umsl.sessionId = ?1 or umsl.isPublic = true")
    List<FavouriteMetadataList> findBySessionOrPublic(String sessionId);

    /**
     *  helper function to find a list by name -
     *
     *  3 cases-
     *      a) Not logged in, and there isn't a session cookie
     *      b) Logged in AND a session cookie (i.e. they created a list while not logged in, then later logged in)
     *      c) User only
     *      d) Session cookie only
     */
    default FavouriteMetadataList findName(String name, User user, String sessionId) {
        if (user == null && sessionId == null) {
            return null;
        }
        if (user !=null && sessionId !=null) {
            return findByNameAndUserOrSessionId(name, user, sessionId);
        }
        if (user != null) {
            return findByNameAndUser(name, user);
        }
        return findByNameAndSessionId(name, sessionId);
    }

    /**
     * this is the main way of finding FavouriteMetadataList available for a user to view.
     * 3 cases-
     *    a) Not logged in, and there isn't a session cookie
     *    b) Logged in AND a session cookie (i.e. they created a list while not logged in, then later logged in)
     *    c) User only
     *    d) Session cookie only
     */
    default List<FavouriteMetadataList> findPublic(User user, String sessionId) {
        if (user == null && sessionId == null) {
            return findPublic();
        }
        if (user !=null && sessionId !=null) {
            return findByUserOrSessionOrPublic(user, sessionId);
        }
        if (user != null) {
            return findByUserOrPublic(user);
        }
        return findBySessionOrPublic(sessionId);
    }

}

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

import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserMetadataSelectionList;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;

public interface UserMetadataSelectionListRepository extends GeonetRepository<UserMetadataSelectionList, Integer>   {

    List<UserMetadataSelectionList> findByUser(User user);
    List<UserMetadataSelectionList> findBySessionId(String sessionId);
    List<UserMetadataSelectionList> findByUserOrSessionId(User user, String sessionId);

    @Query("select umsl from UserMetadataSelectionList umsl where umsl.isPublic = true")
    List<UserMetadataSelectionList> findPublic();

    //---
    @Query("select umsl from UserMetadataSelectionList umsl where umsl.name = ?1 and (umsl.user = ?2 or umsl.sessionId = ?3)")
    UserMetadataSelectionList findByNameAndUserOrSessionId(String name, User user, String sessionId);

    @Query("select umsl from UserMetadataSelectionList umsl where umsl.name = ?1 and umsl.user = ?2")
    UserMetadataSelectionList findByNameAndUser(String name, User user);

    @Query("select umsl from UserMetadataSelectionList umsl where umsl.name = ?1 and  umsl.sessionId = ?2")
    UserMetadataSelectionList findByNameAndSessionId(String name, String sessionId);

    //--
    @Query("select umsl from UserMetadataSelectionList umsl where umsl.user = ?1 or umsl.sessionId = ?2 or umsl.isPublic = true")
    List<UserMetadataSelectionList> findByUserOrSessionOrPublic( User user, String sessionId);

    @Query("select umsl from UserMetadataSelectionList umsl where umsl.user = ?1  or umsl.isPublic = true")
    List<UserMetadataSelectionList> findByUserOrPublic( User user);

    @Query("select umsl from UserMetadataSelectionList umsl where umsl.sessionId = ?1 or umsl.isPublic = true")
    List<UserMetadataSelectionList> findBySessionOrPublic(  String sessionId);

    //--
    default UserMetadataSelectionList findName(String name, User user, String sessionId) {
        if (user == null && sessionId == null) {
            return null;
        }
        if (user !=null && sessionId !=null) {
            return findByNameAndUserOrSessionId(name,user,sessionId);
        }
        if (user != null) {
            return findByNameAndUser(name,user);
        }
        return findByNameAndSessionId(name,sessionId);
    }

    default  List<UserMetadataSelectionList> findPublic(User user, String sessionId) {
        if (user == null && sessionId == null) {
            return findPublic();
        }
        if (user !=null && sessionId !=null) {
            return findByUserOrSessionOrPublic(user,sessionId);
        }
        if (user != null) {
            return findByUserOrPublic(user);
        }
        return findBySessionOrPublic(sessionId);
    }

}

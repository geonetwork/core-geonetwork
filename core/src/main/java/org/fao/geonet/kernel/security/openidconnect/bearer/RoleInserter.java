/*
 * Copyright (C) 2022 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.security.openidconnect.bearer;

import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a simple class that will put roles in the correct location in an OidcUserInfo (basically a Map of claims).
 */
public class RoleInserter {

    /**
     * Puts the roles inside the OidcUserInfo (map) at the given location.
     *
     * For example
     *   _path = "resource_access.gn-key.roles"
     *   userInfo = {"a":"A", ...}
     *   userRoles = ["Reviewer","Editor"]
     *
     *   -->  {"a":"A", ...,
     *   "resource_access": {
     *          "gn-key": {
     *              "roles" : ["Reviewer","Editor"]
     *          }
     *      }
     *   }
     *
     * @param _path     dot-delimited location (ie. "resource_access.gn-key.roles") to put the roles
     * @param userInfo  current info about the user (basically a key-value map)
     * @param userRoles list of the user's roles/groups
     * @return
     */
    public static OidcUserInfo insertRoles(String _path, OidcUserInfo userInfo, List<String> userRoles) {
        String[] paths = _path.trim().split("\\.");
        Map result = new HashMap(userInfo.getClaims());
        Map current = result;
        for (int t = 0; t < paths.length; t++) {
            String path = paths[t];
            boolean lastPathPart = (t == paths.length - 1);
            if (!lastPathPart && current.containsKey(path)) {
                current = (Map) current.get(path);
                continue;
            }

            current.put(path, new HashMap());

            if (!lastPathPart) {
                current.put(path, new HashMap());
                current = (Map) current.get(path);
            } else {
                current.put(path, new ArrayList(userRoles));
            }
        }
        return new OidcUserInfo(result);
    }
}

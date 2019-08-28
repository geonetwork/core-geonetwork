/**
 * Copyright (C) 2001-$today.year Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * <p>
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.listener.security;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.User;
import org.fao.geonet.events.user.UserEvent;

/**
 * Check that the username is correctly scaped.
 *
 * @author delawen
 */
public class CheckUsername {

    public void check(UserEvent event) {
        User user = event.getUser();

        if (StringUtils.contains(user.getUsername(), "<")) {
            user.setUsername(user.getUsername().replaceAll("<", ""));
        }

        if (StringUtils.contains(user.getSurname(), "<")) {
            user.setSurname(user.getSurname().replaceAll("<", ""));
        }

        if (StringUtils.contains(user.getName(), "<")) {
            user.setName(user.getName().replaceAll("<", ""));
        }

        if (StringUtils.contains(user.getOrganisation(), "<")) {
            user.setOrganisation(user.getOrganisation().replaceAll("<", ""));
        }
    }
}

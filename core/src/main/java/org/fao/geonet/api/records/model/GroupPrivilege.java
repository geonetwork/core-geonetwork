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
package org.fao.geonet.api.records.model;

import org.fao.geonet.domain.Profile;

import java.util.List;

/**
 * Created by francois on 16/06/16.
 */
public class GroupPrivilege extends GroupOperations {
    private List<Profile> userProfiles;
    private boolean userGroup;
    private boolean reserved;
    private boolean restricted;

    public List<Profile> getUserProfiles() {
        return userProfiles;
    }

    public void setUserProfile(List<Profile> userProfiles) {
        this.userProfiles = userProfiles;
    }

    public boolean isUserGroup() {
        return userGroup;
    }

    public void setUserGroup(boolean userGroup) {
        this.userGroup = userGroup;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public boolean isRestricted() { return restricted; }

    public void setRestricted(boolean restricted) { this.restricted = restricted; }
}

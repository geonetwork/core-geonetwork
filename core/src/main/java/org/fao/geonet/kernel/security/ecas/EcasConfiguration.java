/*
 * Copyright (C) 2001-2022 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.ecas;

import org.fao.geonet.kernel.security.SecurityProviderConfiguration;

import static org.fao.geonet.kernel.security.SecurityProviderConfiguration.LoginType.*;

/**
 * Some basic configuration info for ecas logins.
 *
 *
 */
public class EcasConfiguration implements SecurityProviderConfiguration {
    private final String SECURITY_PROVIDER = "ECAS";

    private boolean updateProfile;
    private boolean updateGroup;

    public boolean isUpdateProfile() {
        return updateProfile;
    }

    public void setUpdateProfile(boolean updateProfile) {
        this.updateProfile = updateProfile;
    }

    public boolean isUpdateGroup() {
        return updateGroup;
    }

    public void setUpdateGroup(boolean updateGroup) {
        this.updateGroup = updateGroup;
    }

    @Override
    public String getSecurityProvider() {
       return SECURITY_PROVIDER;
    }

    @Override
    public String getLoginType() {
        return LINK.toString();
    }

    @Override
    public boolean isUserProfileUpdateEnabled() {
        // If updating profile from the security provider then disable the profile updates in the interface
        return !updateProfile;
    }

    @Override
    public boolean isUserGroupUpdateEnabled() {
        // If updating group from the security provider then disable the group updates in the interface
        return !updateGroup;
    }
}



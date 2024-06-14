/*
 * Copyright (C) 2024 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security.jwtheaders;

import org.fao.geonet.kernel.security.SecurityProviderConfiguration;
import org.geoserver.security.jwtheaders.JwtConfiguration;

/**
 * configuration for the JWT Headers security filter.
 * See GN documentation.
 * This is based on GeoServer's JWT-Headers Module, so you can see there as well.
 * <p>
 * This class handles the GN filter configuration details, and hands the actual configuration
 * for the filter to the JwtConfiguration class.  This class is also used in Geoserver.
 */
public class JwtHeadersConfiguration {


    public SecurityProviderConfiguration.LoginType loginType = SecurityProviderConfiguration.LoginType.AUTOLOGIN;
    /**
     * true -> update the DB with the information from OIDC (don't allow user to edit profile in the UI)
     * false -> don't update the DB (user must edit profile in UI).
     */
    public boolean updateProfile = true;
    /**
     * true -> update the DB (user's group) with the information from OIDC (don't allow admin to edit user's groups in the UI)
     * false -> don't update the DB (admin must edit groups in UI).
     */
    public boolean updateGroup = true;
    protected JwtConfiguration jwtConfiguration;

    //shared JwtHeadersSecurityConfig object
    JwtHeadersSecurityConfig securityConfig;

    // getters/setters

    public JwtHeadersConfiguration(JwtHeadersSecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
        jwtConfiguration = new JwtConfiguration();
    }

    public boolean isUpdateProfile() {
        return securityConfig.isUpdateProfile();
    }

    public void setUpdateProfile(boolean updateProfile) {
        securityConfig.setUpdateProfile(updateProfile);
    }

    public boolean isUpdateGroup() {
        return securityConfig.isUpdateGroup();
    }


    //---- abstract class methods

    public void setUpdateGroup(boolean updateGroup) {
        securityConfig.setUpdateGroup(updateGroup);
    }

    public String getLoginType() {
        return securityConfig.getLoginType();
    }


    public String getSecurityProvider() {
        return securityConfig.getSecurityProvider();
    }


    public boolean isUserProfileUpdateEnabled() {
        return securityConfig.isUserProfileUpdateEnabled();
    }

    //========================================================================

    // @Override
    public boolean isUserGroupUpdateEnabled() {
        return securityConfig.isUserGroupUpdateEnabled();
    }

    public org.geoserver.security.jwtheaders.JwtConfiguration getJwtConfiguration() {
        return jwtConfiguration;
    }

    public void setJwtConfiguration(
        org.geoserver.security.jwtheaders.JwtConfiguration jwtConfiguration) {
        this.jwtConfiguration = jwtConfiguration;
    }

}

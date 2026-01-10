/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Some basic configuration utils to be used by security providers
 *
 */
public interface SecurityProviderUtil {
    /**
     * Retrieve authentication header value
     * @return the authentication header value. In most cases it should be a bearer token header value. i.e. "Bearer ....."
     */
    String getSSOAuthenticationHeaderValue();

    /**
     * Retrieve user details for the security provider
     * return the user details information
     * @param auth authentication object to get the user details from
     * @return the user details information
     */
    UserDetails getUserDetails(Authentication auth);

    /**
     * Login the service account.  This is used for job processing where there is no user context.
     *
     * @return true if login is successful
     */
    boolean loginServiceAccount();
}



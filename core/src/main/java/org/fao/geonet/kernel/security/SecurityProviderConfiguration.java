/*
 *  Copyright (C) 2014 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fao.geonet.kernel.security;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.exceptions.BadParameterEx;

import java.util.Map;

/**
 * Some basic configuration info to be used by all security
 *
 */
public interface SecurityProviderConfiguration {

	public enum LoginType {
		/**
		 * Default - which will require the provider to set the default
		 */
		DEFAULT,

		/**
		 * Used standard username/password login form
		 */
		FORM,

		/**
		 * Provide link to indicate the user wants to login or logout. This will generally redirect login request to 3rd party authentication.
		 */
		LINK,

		/**
		 * No login form provided which will automatically login the user when possible
		 */
		AUTOLOGIN;

		// ------------------------------------------------------------------------

		public static SecurityProviderConfiguration.LoginType parse(String type) throws BadInputEx {
			if (type == null || type.equalsIgnoreCase("default"))
				return DEFAULT;
			if (type.equalsIgnoreCase("form"))
				return FORM;
			if (type.equalsIgnoreCase("link"))
				return LINK;
			if (type.equalsIgnoreCase("autologin"))
				return AUTOLOGIN;

			throw new BadParameterEx("LoginType", type);
		}

		// ------------------------------------------------------------------------

		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	String getLoginType();

	String getSecurityProvider();

	/**
	 * Check if sso is enabled
	 */
	static SecurityProviderConfiguration get() {
		Map<String, SecurityProviderConfiguration> securityProviderConfigurations = ApplicationContextHolder.get().getBeansOfType(SecurityProviderConfiguration.class);

		if (securityProviderConfigurations != null && securityProviderConfigurations.size() != 0) {
			if (securityProviderConfigurations.size() != 1) {
				throw new RuntimeException("Too many security providers");
			}
			return securityProviderConfigurations.get(securityProviderConfigurations.keySet().toArray()[0]);
		}
		// If we cannot find SecurityProviderConfiguration then default to null.
		return null;
	}

    /**
     * Check if the user profile should be updatable.
     * If the data is coming from the security providers then the security provider then it may make sense to disable the profile update
     */
    boolean isUserProfileUpdateEnabled();

    /**
     * Check if the user group should be updatable.
     * If the data is coming from the security providers then the security provider then it may make sense to disable the user group updates
     */
    boolean isUserGroupUpdateEnabled();

    static SecurityProviderUtil getSecurityProviderUtil() {
        Map<String, SecurityProviderUtil> SecurityProviderUtils = ApplicationContextHolder.get().getBeansOfType(SecurityProviderUtil.class);

        if (SecurityProviderUtils != null && SecurityProviderUtils.size() != 0) {
            if (SecurityProviderUtils.size() != 1) {
                throw new RuntimeException("Too many security providers utils");
            }
            return SecurityProviderUtils.get(SecurityProviderUtils.keySet().toArray()[0]);
        }
        // If we cannot find SecurityProviderUtil then default to null.
        return null;
    }
}



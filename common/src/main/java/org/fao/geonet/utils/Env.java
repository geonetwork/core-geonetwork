//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.utils;

import org.apache.commons.lang.StringUtils;

public final class Env {

    /**
     * Retrieves an environment variable with this priority:
     * - Java environment variable.
     * - System environment variable.
     * - Default value provided as parameter.
     *
     * @param propertyName
     * @param defaultValue
     * @return
     */
    public static String getPropertyFromEnv(String propertyName, String defaultValue) {
        // Check if provided in Java environment variable
        String propertyValue = System.getProperty(propertyName);

        if (StringUtils.isEmpty(propertyValue)) {
            // System environment variable
            propertyValue = System.getenv(propertyName.toUpperCase().replace('.', '_'));
        }

        if (StringUtils.isEmpty(propertyValue)) {
            propertyValue = defaultValue;
        }

        return propertyValue;
    }

}

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

package org.fao.geonet.lib;

import java.sql.Connection;
import java.sql.SQLException;

public enum DatabaseType {
    defaultType(null), sqlserver("jdbc:sqlserver:"), postgis("jdbc:postgresql_postGIS:"), postgres("jdbc:postgresql:"), mysql("jdbc:mysql:"), db2("jdbc:db2:"), mckoi("jdbc:mckoi:"), oracle("jdbc:oracle:");

    private final String urlPrefix;

    private DatabaseType(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public static DatabaseType lookup(Connection dbms) throws SQLException {
        String url = dbms.getMetaData().getURL();
        if (url == null) {
            return defaultType;
        }
        for (DatabaseType dbType : values()) {
            if (dbType != defaultType && url.startsWith(dbType.urlPrefix)) {
                return dbType;
            }
        }

        return defaultType;
    }

    @Override
    public String toString() {
        if (this == defaultType) {
            return "default";
        } else {
            return super.toString();
        }
    }
}

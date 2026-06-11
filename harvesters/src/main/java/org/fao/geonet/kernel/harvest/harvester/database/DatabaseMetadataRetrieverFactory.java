//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.database;

import org.fao.geonet.Logger;

class DatabaseMetadataRetrieverFactory {

    private DatabaseMetadataRetrieverFactory() {
        // Prevent creating instances
    }

    public static DatabaseMetadataRetriever getDatabaseMetadataRetriever(String dbType, String server, int port,
                                                                String database, String username, String password,
                                                                Logger log) throws DatabaseMetadataRetrieverException {

        DatabaseMetadataRetriever metadataRetriever;

        if (dbType.equalsIgnoreCase("postgresql")) {
            String connectionString = "jdbc:postgresql://" + server + ":" + port + "/" + database;
            String driverName = "org.postgresql.Driver";

            metadataRetriever = new DatabaseMetadataRetriever(driverName, connectionString, username, password, log);
        } else if (dbType.equalsIgnoreCase("oracle")) {
            String connectionString = String.format("jdbc:oracle:thin:@%s:%d:%s", server, port, database);
            String driverName = "oracle.jdbc.driver.OracleDriver";

            metadataRetriever = new DatabaseMetadataRetriever(driverName, connectionString, username, password, log);
        } else {
            throw new DatabaseMetadataRetrieverException("Connection for database type " + dbType + " not supported");
        }

        return metadataRetriever;
    }
}

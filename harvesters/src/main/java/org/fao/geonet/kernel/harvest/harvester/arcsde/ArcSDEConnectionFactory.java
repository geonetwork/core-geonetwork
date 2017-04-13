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
package org.fao.geonet.kernel.harvest.harvester.arcsde;

import org.fao.geonet.arcgis.ArcSDEApiConnection;
import org.fao.geonet.arcgis.ArcSDEConnection;
import org.fao.geonet.arcgis.ArcSDEConnectionException;
import org.fao.geonet.arcgis.ArcSDEOracleJdbcConnection;
import org.fao.geonet.arcgis.ArcSDEPostgresJdbcConnection;
import org.fao.geonet.arcgis.ArcSDESqlServerJdbcConnection;
import org.springframework.stereotype.Component;

/**
 * Factory for creating {@link org.fao.geonet.arcgis.ArcSDEConnection} instances.
 *
 * @author juanluisrp on 20/02/2017.
 */
@Component
public class ArcSDEConnectionFactory {

    /**
     * Return the right {@link ArcSDEConnection} implementation for the passed connectionType.
     *
     * @param connectionType the type of the connection. See {@link ArcSDEConnectionType}.
     * @param server         name of the server to connect to (database or ArcSDE).
     * @param port           the port number that the ArcSDE server or database is listening on.
     * @param database       the specific database to connect to. Only applicable to certain databases.
     *                       Value ignored if not applicable.
     * @param username       name of a valid database user account.
     * @param password       the database user's password.
     * @return a specific implementation of {@link ArcSDEConnection} for the passed connection type.
     * @throws ArcSDEConnectionException if the connectionType is not supported
     */
    public ArcSDEConnection getConnection(ArcSDEConnectionType connectionType, String dbType, String server, int port,
                                          String database, String username, String password) throws ArcSDEConnectionException {
        ArcSDEConnection connection;

        switch (connectionType) {
            case ARCSDE:
                connection = new ArcSDEApiConnection(server, port, database, username, password);
                break;
            case JDBC:
                if (dbType.equalsIgnoreCase("oracle")) {
                    connection = new ArcSDEOracleJdbcConnection(server, port, database, username, password);
                } else if (dbType.equalsIgnoreCase("sqlserver")) {
                    connection = new ArcSDESqlServerJdbcConnection(server, port, database, username, password);
                } else if (dbType.equalsIgnoreCase("postgresql")) {
                    connection = new ArcSDEPostgresJdbcConnection(server, port, database, username, password);
                } else {
                    throw new ArcSDEConnectionException("ArcSDEConnectionType " + connectionType.name() + " for database type " + dbType + " not supported");
                }
                break;
            default:
                throw new ArcSDEConnectionException("ArcSDEConnectionType " + connectionType.name() + " not supported");
        }

        return connection;
    }
}

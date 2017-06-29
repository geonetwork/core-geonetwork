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
package org.fao.geonet.arcgis;

/**
 * Created by juanl on 17/02/2017.
 */
public class ArcSDESqlServerJdbcConnection extends ArcSDEJdbcConnection {

    /**
     * Opens a connection to the specified ArcSDE server database.
     * <p>
     * An example of server string in case of jdbc is:
     * "jdbc:oracle:thin:@84.123.79.19:1521:orcl".
     *
     * @param server   the database host.
     * @param port     the database port.
     * @param database
     * @param username }
     * @param password
     */
    public ArcSDESqlServerJdbcConnection(String server, int port, String database, String username, String password) {
        this("jdbc:sqlserver://" + server + ":" + port + ";databaseName=" + database, username, password);
    }


    /**
     * Create a new ArcSDE direct database connection
     * .
     *
     * @param connectionString An example of server string using the thing driver is
     *                         "jdbc:sqlserver://84.123.79.19:1521:1443;databaseName=test"
     * @param username
     * @param password
     */
    public ArcSDESqlServerJdbcConnection(String connectionString, String username, String password) {
        super("com.microsoft.sqlserver.jdbc.SQLServerDriver", connectionString, username, password);
    }
}

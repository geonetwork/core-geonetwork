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

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.dbcp2.BasicDataSource;
import org.fao.geonet.utils.Log;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Created by juanl on 17/02/2017.
 */
public abstract class ArcSDEJdbcConnection implements ArcSDEConnection {

    private BasicDataSource dataSource;
    protected Connection jdbcConnection;
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     *
     *
     * @param connectionString An example of server string in case of Oracle
     *                         is: "jdbc:oracle:thin:@84.123.79.19:1521:orcl".
     * @param username the username to connect to the database.
     * @param password the password to connect to the database.
     */
    public ArcSDEJdbcConnection(String driverName, String connectionString, String username, String password) {

        try {
            Log.debug(ARCSDE_LOG_MODULE_NAME, "Getting ArcSDE connection (via JDBC)");

            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(driverName);
            dataSource.setUrl(connectionString);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            // Test the connection config getting a connection and closing it.
            dataSource.getConnection().close();

            jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        } catch (SQLException x) {
            Log.error(ARCSDE_LOG_MODULE_NAME, "Error getting ArcSDE connection (via JDBC)", x);

            throw new ExceptionInInitializerError(new ArcSDEConnectionException("Exception in ArcSDEConnection using JDBC: can not connect to the database", x));
        }
    }

    /**
     * Closes the connection to the ArcSDE server.
     */
    public void close() throws ArcSDEConnectionException {
        try {
            dataSource.close();
        } catch (SQLException ex) {
            Log.error(ARCSDE_LOG_MODULE_NAME, "Error closing the ArcSDE connection (via JDBC)", ex);
            throw new ArcSDEConnectionException("Exception closing JDBC connection", ex);
        }
    }

    /**
     * Closes the connection to ArcSDE server in case users of this class neglect to do so.
     */
    public void finalize() throws Throwable {
        try {
            if (dataSource != null) {
                dataSource.close();
            }
        } catch (SQLException ex) {
            Log.error(ARCSDE_LOG_MODULE_NAME, "Error closing the ArcSDE connection (via JDBC) "
                + "in finalize method", ex);
            throw new ArcSDEConnectionException("Exception finalizing class ArcSDEConnection", ex);
        } finally {
            super.finalize();
        }
    }

    protected NamedParameterJdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }


    @Override
    public Map<String, String> retrieveMetadata(AtomicBoolean cancelMonitor, String arcSDEVersion) throws Exception {
        Map<String, String> results = new HashMap<>();

        ArcSDEVersionFactory arcSDEVersionFactory = new ArcSDEVersionFactory();
        String metadataTable = arcSDEVersionFactory.getTableName(arcSDEVersion);
        String columnName = arcSDEVersionFactory.getMetadataColumnName(arcSDEVersion);

        String sqlQuery = "SELECT " + columnName + ", UUID FROM " + metadataTable;


        getJdbcTemplate().query(sqlQuery, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // Cancel processing
                if (cancelMonitor.get()) {
                    Log.warning(ARCSDE_LOG_MODULE_NAME, "Cancelling metadata retrieve using "
                            + "ArcSDE connection (via JDBC)");
                    rs.getStatement().cancel();
                    results.clear();
                }

                String document = "";
                int colId = rs.findColumn(columnName);
                int colIdUuid = rs.findColumn("UUID");
                // very simple type check:
                if (rs.getObject(colId) != null) {
                    if (rs.getMetaData().getColumnType(colId) == Types.BLOB) {
                        Blob blob = rs.getBlob(columnName);
                        byte[] bdata = blob.getBytes(1, (int) blob.length());
                        document = new String(bdata);

                    } else if (rs.getMetaData().getColumnType(colId) == Types.LONGVARBINARY) {
                        byte[] byteData = rs.getBytes(colId);
                        document = new String(byteData);

                    } else if (rs.getMetaData().getColumnType(colId) == Types.LONGNVARCHAR ||
                            rs.getMetaData().getColumnType(colId) == Types.LONGVARCHAR ||
                            rs.getMetaData().getColumnType(colId) == Types.VARCHAR ||
                            rs.getMetaData().getColumnType(colId) == Types.SQLXML) {
                        document = rs.getString(colId);

                    } else {
                        throw new SQLException("Trying to harvest from a column with an invalid datatype: " +
                                rs.getMetaData().getColumnTypeName(colId));
                    }

                    String uuid =  rs.getString(colIdUuid);;
                    results.put(uuid, document);
                }

            }
        });

        Log.info(ARCSDE_LOG_MODULE_NAME, "Finished retrieving metadata, found: #" + results.size()
                + " metadata records");

        return results;
    }

}

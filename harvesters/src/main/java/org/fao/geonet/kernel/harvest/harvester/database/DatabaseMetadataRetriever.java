//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import org.apache.commons.dbcp2.BasicDataSource;
import org.fao.geonet.Logger;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;

class DatabaseMetadataRetriever {
    private NamedParameterJdbcTemplate jdbcTemplate;

    protected Logger log;

    /**
     * Constructor.
     *
     * @param connectionString An example of server string in case of Oracle
     *                         is: "jdbc:oracle:thin:@84.123.79.19:1521:orcl".
     * @param username the username to connect to the database.
     * @param password the password to connect to the database.
     */
    public DatabaseMetadataRetriever(String driverName, String connectionString, String username, String password, Logger log) {

        try {
            log.debug("Getting database connection (via JDBC)");

            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(driverName);
            dataSource.setUrl(connectionString);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            // Test the connection config getting a connection and closing it.
            dataSource.getConnection().close();

            jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

            this.log = log;
        } catch (SQLException x) {
            log.error("Error getting database connection", x);

            throw new ExceptionInInitializerError(new DatabaseMetadataRetrieverException("Exception in getting database connection: can not connect to the database", x));
        }
    }

    protected NamedParameterJdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }


    /**
     * Retrieves and process each metadata with the harvester aligner.
     *
     * @param cancelMonitor
     * @param params
     * @param aligner
     * @throws Exception
     */
    public void processMetadata(AtomicBoolean cancelMonitor, DatabaseHarvesterParams params, DatabaseHarvesterAligner aligner) throws Exception {
        String metadataTable = params.getTableName();
        String columnName = params.getMetadataField();
        String filterField = params.getFilterField();
        String filterValue = params.getFilterValue();
        String filterOperator = params.getFilterOperator();

        String sqlOperator = "LIKE";
        if (!StringUtils.isEmpty(filterOperator)) {
            if (filterOperator.equalsIgnoreCase("NOTLIKE")) {
                sqlOperator = "NOT " + sqlOperator;
            }
        }

        String sqlQuery;
        SqlParameterSource param = new MapSqlParameterSource();

        if (StringUtils.hasLength(filterField) && StringUtils.hasLength(filterValue)) {
            sqlQuery = String.format("SELECT %s FROM %s WHERE %s %s :filter", columnName, metadataTable, filterField, sqlOperator);
            param = new MapSqlParameterSource("filter", filterValue);
        } else {
            sqlQuery = String.format("SELECT %s FROM %s", columnName, metadataTable);
        }

        getJdbcTemplate().query(sqlQuery, param, rs -> {
            // Cancel processing
            if (cancelMonitor.get()) {
                log.warning("Cancelling metadata retrieve using database connection");
                rs.getStatement().cancel();
            }

            String document;
            int colId = rs.findColumn(columnName);
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

                aligner.align(document);
            }
        });
    }
}

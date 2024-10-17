/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package v445;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.migration.DatabaseMigrationException;
import org.fao.geonet.utils.Log;
import org.springframework.util.StringUtils;


import java.sql.*;

public class DoiServerDatabaseMigration extends DatabaseMigrationTask {
    @Override
    public void update(Connection connection) throws SQLException, DatabaseMigrationException {
        Log.debug(Geonet.DB, "DoiServerDatabaseMigration");

        boolean doiEnabled = false;
        String doiUrl = "";
        String doiUsername = "";
        String doiPassword = "";
        String doiKey = "";
        String doiLandingPageTemplate = "";
        String doiPublicUrl = "";
        String doiPattern = "";

        try (Statement statement = connection.createStatement()) {
            final String selectDoiSerttingsSQL = "SELECT name, value FROM Settings WHERE name LIKE 'system/publication/doi%'";

            String columnForName = "name";
            String columnForValue = "value";

            final ResultSet resultSet = statement.executeQuery(selectDoiSerttingsSQL);
            while (resultSet.next()) {
                if (resultSet.getString(columnForName).equalsIgnoreCase("system/publication/doi/doienabled")) {
                    doiEnabled = resultSet.getString(columnForValue).equalsIgnoreCase("true");
                } else if (resultSet.getString(columnForName).equalsIgnoreCase("system/publication/doi/doiurl")) {
                    doiUrl = resultSet.getString(columnForValue);
                } else if (resultSet.getString(columnForName).equalsIgnoreCase("system/publication/doi/doiusername")) {
                    doiUsername = resultSet.getString(columnForValue);
                } else if (resultSet.getString(columnForName).equalsIgnoreCase("system/publication/doi/doipassword")) {
                    doiPassword = resultSet.getString(columnForValue);
                } else if (resultSet.getString(columnForName).equalsIgnoreCase("system/publication/doi/doikey")) {
                    doiKey = resultSet.getString(columnForValue);
                } else if (resultSet.getString(columnForName).equalsIgnoreCase("system/publication/doi/doilandingpagetemplate")) {
                    doiLandingPageTemplate = resultSet.getString(columnForValue);
                } else if (resultSet.getString(columnForName).equalsIgnoreCase("system/publication/doi/doipublicurl")) {
                    doiPublicUrl = resultSet.getString(columnForValue);
                } else if (resultSet.getString(columnForName).equalsIgnoreCase("system/publication/doi/doipattern")) {
                    doiPattern = resultSet.getString(columnForValue);
                }

            }
        }

        if (doiEnabled) {

            // Check the information is filled
            boolean createDoiServer = StringUtils.hasLength(doiUrl) &&
                StringUtils.hasLength(doiUsername) &&
                StringUtils.hasLength(doiPassword) &&
                StringUtils.hasLength(doiKey) &&
                StringUtils.hasLength(doiPattern);

            if (createDoiServer) {
                try (PreparedStatement update = connection.prepareStatement(
                    "INSERT INTO doiservers " +
                        "(id, isdefault, landingpagetemplate, name, url, username, password, pattern, prefix, publicurl) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                ) {

                    update.setInt(1, 1);
                    update.setString(2, "y");
                    update.setString(3, doiLandingPageTemplate);
                    update.setString(4, "Default DOI server");
                    update.setString(5, doiUrl);
                    update.setString(6, doiUsername);
                    update.setString(7, doiPassword);
                    update.setString(8, doiPattern);
                    update.setString(9, doiKey);
                    update.setString(10, doiPublicUrl);

                    update.execute();

                } catch (java.sql.BatchUpdateException e) {
                    connection.rollback();
                    Log.error(Geonet.GEONETWORK, "Error occurred while creating the DOI server:" + e.getMessage(), e);
                    SQLException next = e.getNextException();
                    while (next != null) {
                        Log.error(Geonet.GEONETWORK, "Next error: " + next.getMessage(), next);
                        next = e.getNextException();
                    }

                    throw new RuntimeException(e);
                } catch (Exception e) {
                    connection.rollback();

                    throw new Error(e);
                }


                try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM Settings WHERE name LIKE 'system/publication/doi%' and name != 'system/publication/doi/doienabled'")
                ) {
                    delete.execute();
                } catch (java.sql.BatchUpdateException e) {
                    connection.rollback();
                    Log.error(Geonet.GEONETWORK, "Error occurred while creating the DOI server:" + e.getMessage(), e);
                    SQLException next = e.getNextException();
                    while (next != null) {
                        Log.error(Geonet.GEONETWORK, "Next error: " + next.getMessage(), next);
                        next = e.getNextException();
                    }

                    throw new RuntimeException(e);
                } catch (Exception e) {
                    connection.rollback();

                    throw new Error(e);
                }

                connection.commit();

                Log.info(Geonet.DB, "Migration: migrated DOI server");
            }
        }
    }
}

package v300;

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

//package v300;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SetSequenceValueToMaxOfMetadataAndStats extends DatabaseMigrationTask {
    @Override
    public void update(Connection connection) throws SQLException {
        Log.debug(Geonet.DB, "SetSequenceValueToMaxOfMetadataAndStats");

        try (Statement statement = connection.createStatement()) {
            final String numberOfMetadataSQL = "SELECT max(id) as NB FROM Metadata";
            final String numberOfParamsSQL = "SELECT max(id) as NB FROM Params";
            final String numberOfRequestsSQL = "SELECT max(id) as NB FROM Requests";

            ResultSet metadataResultSet = statement.executeQuery(numberOfMetadataSQL);
            int numberOfMetadata = 0;
            try {
                if (metadataResultSet.next()) {
                    numberOfMetadata = metadataResultSet.getInt(1);
                }

                Log.debug(Geonet.DB, "  Number of metadata: " + numberOfMetadata);
            } finally {
                metadataResultSet.close();
            }

            int numberOfParams = 0;
            ResultSet paramsResultSet = statement.executeQuery(numberOfParamsSQL);
            try {
                if (paramsResultSet.next()) {
                    numberOfParams = paramsResultSet.getInt(1);
                }
                Log.debug(Geonet.DB, "  Number of params (statistics): " + numberOfParams);
            } finally {
                paramsResultSet.close();
            }

            int numberOfRequests = 0;
            ResultSet requestsResultSet = statement.executeQuery(numberOfRequestsSQL);
            try {
                if (requestsResultSet.next()) {
                    numberOfRequests = requestsResultSet.getInt(1);
                }
                Log.debug(Geonet.DB, "  Number of requests (statistics): " + numberOfRequests);
            } finally {
                requestsResultSet.close();
            }
            try {
                int newSequenceValue = Math.max(numberOfMetadata, Math.max(numberOfParams, numberOfRequests)) + 1;
                Log.debug(Geonet.DB, "  Set sequence to value: " + newSequenceValue);
                final String updateSequenceSQL = "ALTER SEQUENCE HIBERNATE_SEQUENCE " +
                    "RESTART WITH " + newSequenceValue;
                statement.execute(updateSequenceSQL);

                // TODO: Probably a scenario for Oracle db
                // ALTER sequence HIBERNATE_SEQUENCE increment by X;
                // select seq1.nextval from dual;
                // ALTER sequence HIBERNATE_SEQUENCE increment by 1;
                Log.debug(Geonet.DB, "  Sequence updated.");
            } catch (Exception e) {
                Log.debug(Geonet.DB, "  Sequence not updated. Error is: " + e.getMessage());
                Log.error(Geonet.DB, e);
                // On Oracle : To restart the sequence at a different number, you must drop and re-create it.
            }
        } catch (Exception e) {
            Log.debug(Geonet.DB, "  Exception while updating sequence. " +
                "Error is: " + e.getMessage());
            Log.error(Geonet.DB, e);
        }
    }
}

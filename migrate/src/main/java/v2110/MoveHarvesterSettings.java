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

package v2110;

import v280.MoveHarvesterSettingsToHigherNumber;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Jesse on 10/31/2014.
 */
public class MoveHarvesterSettings extends MoveHarvesterSettingsToHigherNumber {
    @Override
    public void update(Connection connection) throws SQLException {
        super.counter.set(100);
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO harvestersettings (ID, NAME) VALUES (2, 'harvesting')");
            statement.execute("INSERT INTO harvestersettings (ID, NAME) VALUES (1, 'harvesting')");
        }
        super.update(connection);
        try (Statement statement = connection.createStatement()) {
            statement.execute("UPDATE harvestersettings SET parentid=1 WHERE parentid=2");
            statement.execute("DELETE FROM harvestersettings WHERE id=2");
        }
    }

    @Override
    protected String getHarvesterSettingsName() {
        return "HarvesterSettings";
    }
}

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

package v280;

import org.fao.geonet.DatabaseMigrationTask;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class MoveHarvesterSettingsToHigherNumber extends DatabaseMigrationTask {
    protected AtomicInteger counter = new AtomicInteger(10000);

    protected String getHarvesterSettingsName() {
        return "Settings";
    }

    @Override
    public void update(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            final String selectHarvestersSQL = "SELECT * FROM Settings WHERE parentId = (SELECT id FROM Settings WHERE name='harvesting' and parentId=0)";

            final ResultSet resultSet = statement.executeQuery(selectHarvestersSQL);

            List<HarvesterSetting> settings = new ArrayList<HarvesterSetting>();
            try {

                while (resultSet.next()) {
                    settings.add(new HarvesterSetting(resultSet.getInt("parentId"), resultSet));
                    counter.addAndGet(200);

                }
            } finally {
                resultSet.close();
            }

            for (HarvesterSetting setting : settings) {
                setting.loadChildren(statement);
            }
            for (HarvesterSetting setting : settings) {
                setting.delete(statement);
            }
            for (HarvesterSetting setting : settings) {
                setting.write(statement);
            }
        }
    }

    private class HarvesterSetting {

        int originalId;
        int id;
        int parentId;
        String name;
        String value;
        List<HarvesterSetting> children = new ArrayList<HarvesterSetting>();

        public HarvesterSetting(int parentId, ResultSet resultSet) throws SQLException {
            id = counter.incrementAndGet();
            this.parentId = parentId;
            this.name = resultSet.getString("name");
            this.value = resultSet.getString("value");
            originalId = resultSet.getInt("id");

        }

        private void loadChildren(Statement statement) throws SQLException {
            final ResultSet resultSet2 = statement.executeQuery("SELECT * FROM Settings where parentId = " + originalId); //NOSONAR
            try {
                while (resultSet2.next()) {
                    children.add(new HarvesterSetting(id, resultSet2));
                }
            } finally {
                resultSet2.close();
            }
            for (HarvesterSetting child : children) {
                child.loadChildren(statement);
            }
        }

        public void write(Statement statement) throws SQLException {
            final String sql = format("INSERT INTO " + getHarvesterSettingsName() + " (id, parentId, name, value) VALUES (%s, %s, " +
                "'%s', '%s')", id, parentId, name, value);
            statement.execute(sql);
            for (HarvesterSetting child : children) {
                child.write(statement);
            }
        }


        public void delete(Statement statement) throws SQLException {
            for (HarvesterSetting child : children) {
                child.delete(statement);
            }
            statement.execute("DELETE FROM Settings WHERE id=" + originalId); // NOSONAR
        }
    }
}

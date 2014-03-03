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

public class MoveHarvesterSettingsToHigherNumber implements DatabaseMigrationTask {
    AtomicInteger counter = new AtomicInteger(10000);

    private class HarvesterSetting {

        int originalId;
        int id;
        int parentId;
        String name;
        String value;
        List<HarvesterSetting> children = new ArrayList<HarvesterSetting>();

        public HarvesterSetting(int parentId, ResultSet resultSet, Statement statement) throws SQLException {
            id = counter.incrementAndGet();
            this.parentId = parentId;
            this.name = resultSet.getString("name");
            this.value = resultSet.getString("value");
            originalId = resultSet.getInt("id");

        }

        private void loadChildren(Statement statement) throws SQLException {
            final ResultSet resultSet2 = statement.executeQuery("SELECT * FROM Settings where parentId = "+originalId);
            try {
                while (resultSet2.next()) {
                    children.add(new HarvesterSetting(id, resultSet2, statement));
                }
            } finally {
                resultSet2.close();
            }
            for (HarvesterSetting child : children) {
                child.loadChildren(statement);
            }
        }

        public void write(Statement statement) throws SQLException {
            statement.execute(format("INSERT INTO Settings (id, parentId, name, value) VALUES (%s, %s, '%s', '%s')", id, parentId, name, value));
            for (HarvesterSetting child : children) {
                child.write(statement);
            }
        }

        public void delete(Statement statement) throws SQLException {
            for (HarvesterSetting child : children) {
                child.delete(statement);
            }
            statement.execute("DELETE FROM Settings WHERE id=" + originalId);
        }
    }

    @Override
    public void update(Statement statement) throws SQLException {
        final String selectHarvestersSQL = "SELECT * FROM Settings WHERE parentId = (SELECT id FROM Settings WHERE name='harvesting' and parentId=0)";
        final ResultSet resultSet = statement.executeQuery(selectHarvestersSQL);

        List<HarvesterSetting> settings = new ArrayList<HarvesterSetting>();
        try {

            while (resultSet.next()) {
                settings.add(new HarvesterSetting(resultSet.getInt("parentId"), resultSet, statement));
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

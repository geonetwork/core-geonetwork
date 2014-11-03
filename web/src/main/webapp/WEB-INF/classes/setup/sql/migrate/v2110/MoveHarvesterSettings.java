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

package v300;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SetSequenceValueToMaxOfMetadataAndStats implements DatabaseMigrationTask {
    @Override
    public void update(Connection connection) throws SQLException {
        Log.debug(Geonet.DB, "SetSequenceValueToMaxOfMetadataAndStats");

        try (Statement statement = connection.createStatement()) {
            final String numberOfMetadataSQL = "SELECT max(id) as NB FROM Metadata";
            final String numberOfParamsSQL = "SELECT max(id) as NB FROM Params";

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

            try {
                int newSequenceValue = Math.max(numberOfMetadata, numberOfParams) + 1;
                Log.debug(Geonet.DB, "  Set sequence to value: " + newSequenceValue);
                final String updateSequenceSQL = "ALTER SEQUENCE HIBERNATE_SEQUENCE " +
                        "RESTART WITH " + newSequenceValue;
                statement.execute(updateSequenceSQL);

                Log.debug(Geonet.DB, "  Sequence updated.");
            } catch (Exception e) {
                Log.debug(Geonet.DB, "  Sequence not updated. Error is: " + e.getMessage());
                e.printStackTrace();
                // On Oracle : To restart the sequence at a different number, you must drop and re-create it.
            }
        } catch (Exception e) {
            Log.debug(Geonet.DB, "  Exception while updating sequence. " +
                    "Error is: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

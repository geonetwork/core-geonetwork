package org.fao.geonet.arcgis;

import org.fao.geonet.utils.Log;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by juanl on 17/02/2017.
 */
public class ArcSDESqlServerJdbcConnection extends ArcSDEJdbcConnection {

    private static final String METADATA_TABLE = "GDB_ITEMS";
    private static final String METADATA_COLUMN = "documentation ";
    private static final String SQL_QUERY = "SELECT " + METADATA_COLUMN + " FROM " + METADATA_TABLE;


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
        this("jdbc:oracle:thin:@" + server + ":" + port + ":" + database, username, password);
    }


    /**
     * Create a new ArcSDE direct database connection
     * .
     *
     * @param connectionString An example of server string using the thing driver is
     *                         "jdbc:oracle:thin:@84.123.79.19:1521:orcl"
     * @param username
     * @param password
     */
    public ArcSDESqlServerJdbcConnection(String connectionString, String username, String password) {
        super(connectionString, username, password);
    }


    @Override
    public List<String> retrieveMetadata(AtomicBoolean cancelMonitor, String arcSdeVersion) throws Exception {
        List<String> results = new ArrayList<>();

        getJdbcTemplate().query(SQL_QUERY, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // Cancel processing
                if (cancelMonitor.get()) {
                    Log.warning(ARCSDE_LOG_MODULE_NAME, "Cancelling metadata retrieve using "
                        + "ArcSDE connection (via Oracle JDBC)");
                    rs.getStatement().cancel();
                    results.clear();
                }

                String document = "";
                int colId = rs.findColumn(METADATA_COLUMN);
                // very simple type check:
                if (rs.getMetaData().getColumnType(colId) == Types.BLOB) {
                    Blob blob = rs.getBlob(METADATA_COLUMN);
                    byte[] bdata = blob.getBytes(1, (int) blob.length());
                    document = new String(bdata);

                } else if (rs.getMetaData().getColumnType(colId) == Types.LONGVARBINARY) {
                    byte[] byteData = rs.getBytes(colId);
                    document = new String(byteData);

                } else {
                    throw new SQLException("Trying to harvest from a column with an invalid datatype: " +
                        rs.getMetaData().getColumnTypeName(colId));
                }
                if (document.contains(ISO_METADATA_IDENTIFIER)) {
                    Log.info(ARCSDE_LOG_MODULE_NAME, "ISO metadata found");
                    results.add(document);
                }
            }
        });

        Log.info(ARCSDE_LOG_MODULE_NAME, "Finished retrieving metadata, found: #" + results.size()
            + " metadata records");

        return results;
    }


}

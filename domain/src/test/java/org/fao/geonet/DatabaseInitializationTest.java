package org.fao.geonet;

import static org.fao.geonet.repository.SpringDataTestSupport.updateNatively;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import org.fao.geonet.repository.AbstractSpringDataTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Function;

public class DatabaseInitializationTest extends AbstractSpringDataTest {

    @Autowired
    ApplicationContext _appContext;

    @Test
    public void testTableInitialization() throws SQLException {
        updateNatively(_appContext, new Function<Statement, Void>() {

            @Override
            @Nullable
            public Void apply(@Nullable Statement input) {
                try {
                    checkTables(input);
                } catch (AssertionError e) {
                    throw e;
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
                return null;
            }
        });
    }

    protected void checkTables(Statement statement) throws Throwable {
        String query = "SELECT * from INFORMATION_SCHEMA.TABLES";
        List<String[]> results = toList(statement, query, "TABLE_SCHEMA", "TABLE_NAME");
        ArrayList<String> tablesEncountered = new ArrayList<String>(ALL_TABLES.length);

        for (String[] row : results) {
            if ("public".equalsIgnoreCase(row[0])) {
                tablesEncountered.add(row[1].toUpperCase());
            }
        }

        compareSameElements("Tables", ALL_TABLES, tablesEncountered);
    }

    private void compareSameElements(String type, String[] expected, ArrayList<String> actual) {
        HashSet<String> fromAll = new HashSet<String>(Arrays.asList(expected));
        fromAll.removeAll(actual);
        actual.removeAll(Arrays.asList(expected));
        String msg = "The following " + type + " are not created by JPA but should have been: " + fromAll + "\nThe following "+type+" were created but should NOT have been";
        assertTrue(msg, fromAll.isEmpty());


        assertTrue(msg, actual.isEmpty());

        assertArrayEquals(type + " are not the same", ALL_TABLES, actual.toArray(new String[actual.size()]));
    }

    private List<String[]> toList(Statement statement, String query, String... columnsToRetrieve) throws Throwable {
        ResultSet rs = statement.executeQuery(query);
        ArrayList<String[]> results = new ArrayList<String[]>();
        while (rs.next()) {
            String[] row = new String[columnsToRetrieve.length];
            for (int i = 0; i < row.length; i++) {
                row[i] = rs.getString(columnsToRetrieve[i]);
            }
            results.add(row);
        }

        return results;
    }

    String[] ALL_TABLES = { "CATEGORIES", "CATEGORIESDES", "CSWSERVERCAPABILITIESINFO", "CUSTOMELEMENTSET", "GROUPS", "GROUPSDES",
            "HARVESTHISTORY", "ISOLANGUAGES", "ISOLANGUAGESDES", "LANGUAGES", "METADATA", "METADATACATEG", "METADATANOTIFICATIONS",
            "METADATANOTIFIERS", "METADATARATING", "METADATASTATUS", "OPERATIONALLOWED", "OPERATIONS", "OPERATIONSDES", "PARAMS",
            "RELATIONS", "REQUESTS", "SERVICEPARAMETERS", "SERVICES", "SETTINGS", "SOURCES", "STATUSVALUES", "STATUSVALUESDES",
            "THESAURUS", "USERGROUPS", "USERS", "VALIDATION" };
}

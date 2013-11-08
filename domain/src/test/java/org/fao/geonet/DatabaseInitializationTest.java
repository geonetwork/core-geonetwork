package org.fao.geonet;

import com.google.common.base.Function;
import org.fao.geonet.domain.Constants;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.fao.geonet.Column.column;
import static org.fao.geonet.repository.SpringDataTestSupport.updateNatively;

@Transactional
public class DatabaseInitializationTest extends AbstractSpringDataTest {
    public final String SCHEMA = "PUBLIC";

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
                    checkColumns(input);
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

    protected void checkColumns(Statement input) throws Throwable {
        final String TABLE_NAME = "TABLE_NAME";
        final String COLUMN_NAME = "COLUMN_NAME";
        final String COLUMN_DEFAULT = "COLUMN_DEFAULT";
        final String IS_NULLABLE = "IS_NULLABLE";
        final String TYPE_NAME = "TYPE_NAME";
        final String CHARACTER_MAXIMUM_LENGTH = "CHARACTER_MAXIMUM_LENGTH";

        String query = String.format(
                "SELECT %s, %s, %s, %s, %s, %s from INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='%s' ORDER BY TABLE_NAME", TABLE_NAME,
                COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, TYPE_NAME, CHARACTER_MAXIMUM_LENGTH, SCHEMA);
        List<Column> columns = new ArrayList<Column>();
        for (String[] column : toList(input, query, TABLE_NAME, COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, TYPE_NAME,
                CHARACTER_MAXIMUM_LENGTH)) {
            columns.add(column(column[0], column[1], column[2], column[3], column[4], column[5]));
        }

        compareSameElements("Columns", FROM_COLUMNS_TABLE, columns, false);
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

        compareSameElements("Tables", ALL_TABLES, tablesEncountered, false);
    }

    private <T extends Comparable<T>> void compareSameElements(String type, T[] expected, List<T> actual, boolean showAllInErrorMessage) {
        Collections.sort(actual);
        Arrays.sort(expected);

        List<String> failures = new ArrayList<String>();
        for (int i = 0; i < expected.length && i < actual.size(); i++) {
            if (!expected[i].equals(actual.get(i))) {
                failures.add("\n" + expected[i] + "\n" + actual.get(i) + "\n");
            }
        }

        if (!failures.isEmpty()) {
            String msg = failures.toString();
            if (showAllInErrorMessage) {
                int max = Math.max(actual.size(), expected.length);
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < max; i++) {
                    String ne = i < expected.length ? expected[i].toString() : "";
                    String na = i < actual.size() ? actual.get(i).toString() : "";
                    b.append('\n');
                    b.append(ne);
                    for (int j = 0; j < 30 - ne.length(); j++) {
                        b.append(' ');
                    }
                    b.append(na);
                }
                msg = b.toString();
            }
            throw new AssertionError(msg);
        }

        if (expected.length < actual.size()) {
            List<T> extraColumns = actual.subList(expected.length - 1, actual.size());
            throw new AssertionError("New columns: \n" + extraColumns.toString().replace(',', '\n'));
        } else if (expected.length > actual.size()) {
            List<T> missingColumns = Arrays.asList(expected).subList(actual.size() - 1, expected.length);
            throw new AssertionError("Missing columns: \n" + missingColumns.toString().replace(',', '\n'));
        }
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

    String[] ALL_TABLES = {"ADDRESS", "EMAIL", "CATEGORIES", "CATEGORIESDES", "CSWSERVERCAPABILITIESINFO", "CUSTOMELEMENTSET",
            "GROUPS", "GROUPSDES",
            "HARVESTERSETTINGS", "HARVESTHISTORY", "ISOLANGUAGES", "ISOLANGUAGESDES", "LANGUAGES", "METADATA", "METADATACATEG",
            "METADATANOTIFICATIONS",
            "METADATANOTIFIERS", "METADATARATING", "METADATASTATUS", "OPERATIONALLOWED", "OPERATIONS", "OPERATIONSDES", "PARAMS",
            "RELATIONS", "REQUESTS", "SERVICEPARAMETERS", "SERVICES", "SETTINGS", "SOURCES", "STATUSVALUES", "STATUSVALUESDES",
            "THESAURUS", "USERGROUPS", "USERS", "USERADDRESS", "VALIDATION"};
    Column[] FROM_COLUMNS_TABLE = new Column[]{column("CATEGORIES", "NAME", "<null>", "NO", "VARCHAR", "255"),
            column("CATEGORIES", "ID", "<null>", "NO", "INTEGER", "10", true), column("CATEGORIESDES", "LANGID", "<null>", "NO",
            "VARCHAR", "5"),
            column("CATEGORIESDES", "LABEL", "<null>", "NO", "VARCHAR", "255"),
            column("CATEGORIESDES", "IDDES", "<null>", "NO", "INTEGER", "10"),
            column("CSWSERVERCAPABILITIESINFO", "LANGID", "<null>", "NO", "VARCHAR", "5"),
            column("CSWSERVERCAPABILITIESINFO", "IDFIELD", "<null>", "NO", "INTEGER", "10", true),
            column("CSWSERVERCAPABILITIESINFO", "FIELD", "<null>", "NO", "VARCHAR", "32"),
            column("CSWSERVERCAPABILITIESINFO", "LABEL", "<null>", "YES", "CLOB", "2147483647"),
            column("CUSTOMELEMENTSET", "XPATHHASHCODE", "<null>", "NO", "INTEGER", "10"),
            column("CUSTOMELEMENTSET", "XPATH", "<null>", "NO", "VARCHAR", "1000"),
            column("GROUPS", "REFERRER", "<null>", "YES", "INTEGER", "10"), column("GROUPS", "EMAIL", "<null>", "YES", "VARCHAR", "32"),
            column("GROUPS", "DESCRIPTION", "<null>", "YES", "VARCHAR", "255"), column("GROUPS", "NAME", "<null>", "NO", "VARCHAR", "32"),
            column("GROUPS", "ID", "<null>", "NO", "INTEGER", "10", true), column("GROUPSDES", "LABEL", "<null>", "NO", "VARCHAR", "96"),
            column("GROUPSDES", "LANGID", "<null>", "NO", "VARCHAR", "5"), column("GROUPSDES", "IDDES", "<null>", "NO", "INTEGER", "10"),
            column("HARVESTHISTORY", "HARVESTDATE", "<null>", "YES", "VARCHAR", "30"),
            column("HARVESTHISTORY", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("HARVESTHISTORY", "ELAPSEDTIME", "<null>", "YES", "INTEGER", "10"),
            column("HARVESTHISTORY", "HARVESTERUUID", "<null>", "YES", "VARCHAR", "255"),
            column("HARVESTHISTORY", "HARVESTERNAME", "<null>", "YES", "VARCHAR", "255"),
            column("HARVESTHISTORY", "HARVESTERTYPE", "<null>", "YES", "VARCHAR", "255"),
            column("HARVESTHISTORY", "INFO", "<null>", "YES", "CLOB", "2147483647"),
            column("HARVESTHISTORY", "PARAMS", "<null>", "YES", "CLOB", "2147483647"),
            column("HARVESTHISTORY", "DELETED", "<null>", "NO", "CHAR", "1"),
            column("HARVESTERSETTINGS", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("HARVESTERSETTINGS", "NAME", "<null>", "NO", "VARCHAR", "255"),
            column("HARVESTERSETTINGS", "PARENTID", "<null>", "YES", "INTEGER", "10"),
            column("HARVESTERSETTINGS", "VALUE", "<null>", "YES", "CLOB", "2147483647"),
            column("ISOLANGUAGES", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("ISOLANGUAGES", "CODE", "<null>", "NO", "VARCHAR", "3"),
            column("ISOLANGUAGES", "SHORTCODE", "<null>", "YES", "VARCHAR", "2"),
            column("ISOLANGUAGESDES", "LABEL", "<null>", "NO", "VARCHAR", "255"),
            column("ISOLANGUAGESDES", "LANGID", "<null>", "NO", "VARCHAR", "5"),
            column("ISOLANGUAGESDES", "IDDES", "<null>", "NO", "INTEGER", "10"),
            column("LANGUAGES", "ISDEFAULT", "<null>", "YES", "CHAR", "1"),
            column("LANGUAGES", "ID", "<null>", "NO", "VARCHAR", "5", true),
            column("LANGUAGES", "NAME", "<null>", "NO", "VARCHAR", "255"),
            column("LANGUAGES", "ISINSPIRE", "<null>", "YES", "CHAR", "1"),
            column("METADATA", "RATING", "<null>", "NO", "INTEGER", "10"),
            column("METADATA", "HARVESTURI", "<null>", "YES", "VARCHAR", "512"),
            column("METADATA", "GROUPOWNER", "<null>", "YES", "INTEGER", "10"),
            column("METADATA", "POPULARITY", "<null>", "NO", "INTEGER", "10"),
            column("METADATA", "DISPLAYORDER", "<null>", "YES", "INTEGER", "10"),
            column("METADATA", "DOCTYPE", "<null>", "YES", "VARCHAR", "255"),
            column("METADATA", "OWNER", "<null>", "NO", "INTEGER", "10"),
            column("METADATA", "HARVESTUUID", "<null>", "YES", "VARCHAR", "255"),
            column("METADATA", "ROOT", "<null>", "YES", "VARCHAR", "255"),
            column("METADATA", "TITLE", "<null>", "YES", "VARCHAR", "255"),
            column("METADATA", "SOURCE", "<null>", "NO", "VARCHAR", "255"),
            column("METADATA", "DATA", "<null>", "NO", "CLOB", "2147483647"),
            column("METADATA", "CHANGEDATE", "<null>", "NO", "VARCHAR", "30"),
            column("METADATA", "CREATEDATE", "<null>", "NO", "VARCHAR", "30"),
            column("METADATA", "ISHARVESTED", "<null>", "NO", "CHAR", "1"),
            column("METADATA", "ISTEMPLATE", "<null>", "NO", "CHAR", "1"),
            column("METADATA", "SCHEMAID", "<null>", "NO", "VARCHAR", "32"),
            column("METADATA", "UUID", "<null>", "NO", "VARCHAR", "255"),
            column("METADATA", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("METADATACATEG", "CATEGORYID", "<null>", "NO", "INTEGER", "10"),
            column("METADATACATEG", "METADATAID", "<null>", "NO", "INTEGER", "10"),
            column("METADATANOTIFICATIONS", "ERRORMSG", "<null>", "YES", "CLOB", "2147483647"),
            column("METADATANOTIFICATIONS", "ACTION", "<null>", "NO", "INTEGER", "10"),
            column("METADATANOTIFICATIONS", "METADATAUUID", "<null>", "NO", "VARCHAR", "255"),
            column("METADATANOTIFICATIONS", "METADATAID", "<null>", "NO", "INTEGER", "10"),
            column("METADATANOTIFICATIONS", "NOTIFIERID", "<null>", "NO", "INTEGER", "10"),
            column("METADATANOTIFICATIONS", "NOTIFIED", "<null>", "NO", "CHAR", "1"),
            column("METADATANOTIFIERS", "PASSWORD", "<null>", "YES", "VARCHAR", "255"),
            column("METADATANOTIFIERS", "USERNAME", "<null>", "YES", "VARCHAR", "32"),
            column("METADATANOTIFIERS", "ENABLED", "<null>", "NO", "CHAR", "1"),
            column("METADATANOTIFIERS", "URL", "<null>", "NO", "VARCHAR", "255"),
            column("METADATANOTIFIERS", "NAME", "<null>", "NO", "VARCHAR", "32"),
            column("METADATANOTIFIERS", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("METADATARATING", "METADATAID", "<null>", "NO", "INTEGER", "10"),
            column("METADATARATING", "IPADDRESS", "<null>", "NO", "VARCHAR", "" + Constants.IP_ADDRESS_COLUMN_LENGTH),
            column("METADATARATING", "RATING", "<null>", "NO", "INTEGER", "10"),
            column("METADATASTATUS", "CHANGEMESSAGE", "<null>", "NO", "VARCHAR", "2048"),
            column("METADATASTATUS", "CHANGEDATE", "<null>", "NO", "VARCHAR", "30"),
            column("METADATASTATUS", "USERID", "<null>", "NO", "INTEGER", "10"),
            column("METADATASTATUS", "STATUSID", "<null>", "NO", "INTEGER", "10"),
            column("METADATASTATUS", "METADATAID", "<null>", "NO", "INTEGER", "10"),
            column("OPERATIONALLOWED", "GROUPID", "<null>", "NO", "INTEGER", "10"),
            column("OPERATIONALLOWED", "METADATAID", "<null>", "NO", "INTEGER", "10"),
            column("OPERATIONALLOWED", "OPERATIONID", "<null>", "NO", "INTEGER", "10"),
            column("OPERATIONS", "NAME", "<null>", "NO", "VARCHAR", "32"),
            column("OPERATIONS", "ID", "<null>", "NO", "INTEGER", "10", true), column("OPERATIONSDES", "LABEL", "<null>", "NO",
            "VARCHAR", "255"),
            column("OPERATIONSDES", "IDDES", "<null>", "NO", "INTEGER", "10"),
            column("OPERATIONSDES", "LANGID", "<null>", "NO", "VARCHAR", "5"),
            column("PARAMS", "REQUESTID", "<null>", "YES", "INTEGER", "10"),
            column("PARAMS", "QUERYTYPE", "<null>", "YES", "INTEGER", "10"),
            column("PARAMS", "TERMFIELD", "<null>", "YES", "VARCHAR", "255"),
            column("PARAMS", "TERMTEXT", "<null>", "YES", "VARCHAR", "255"),
            column("PARAMS", "SIMILARITY", "<null>", "NO", "DOUBLE", "17"),
            column("PARAMS", "LOWERTEXT", "<null>", "YES", "VARCHAR", "255"),
            column("PARAMS", "UPPERTEXT", "<null>", "YES", "VARCHAR", "255"), column("PARAMS", "INCLUSIVE", "<null>", "YES", "CHAR", "1"),
            column("PARAMS", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("RELATIONS", "ID", "<null>", "NO", "INTEGER", "10"),
            column("RELATIONS", "RELATEDID", "<null>", "NO", "INTEGER", "10"),
            column("REQUESTS", "AUTOGENERATED", "<null>", "NO", "BOOLEAN", "1"),
            column("REQUESTS", "HITS", "<null>", "NO", "INTEGER", "10"),
            column("REQUESTS", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("REQUESTS", "IP", "<null>", "YES", "VARCHAR", "" + Constants.IP_ADDRESS_COLUMN_LENGTH),
            column("REQUESTS", "LANG", "<null>", "YES", "VARCHAR", "16"),
            column("REQUESTS", "QUERY", "<null>", "YES", "CLOB", "2147483647"),
            column("REQUESTS", "REQUESTDATE", "<null>", "YES", "VARCHAR", "30"),
            column("REQUESTS", "SERVICE", "<null>", "YES", "VARCHAR", "255"),
            column("REQUESTS", "SIMPLE", "<null>", "NO", "BOOLEAN", "1"),
            column("REQUESTS", "SORTBY", "<null>", "YES", "VARCHAR", "255"),
            column("REQUESTS", "SPATIALFILTER", "<null>", "YES", "CLOB", "2147483647"),
            column("REQUESTS", "TYPE", "<null>", "YES", "CLOB", "2147483647"),
            column("SERVICEPARAMETERS", "NAME", "<null>", "NO", "VARCHAR", "255"),
            column("SERVICEPARAMETERS", "SERVICE", "<null>", "NO", "INTEGER", "10"),
            column("SERVICEPARAMETERS", "VALUE", "<null>", "NO", "VARCHAR", "1024"),
            column("SERVICES", "CLASS", "<null>", "NO", "VARCHAR", "1024"),
            column("SERVICES", "DESCRIPTION", "<null>", "YES", "VARCHAR", "1024"),
            column("SERVICES", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("SERVICES", "NAME", "<null>", "NO", "VARCHAR", "255"),
            column("SETTINGS", "NAME", "<null>", "NO", "VARCHAR", "255"),
            column("SETTINGS", "VALUE", "<null>", "YES", "CLOB", "2147483647"),
            column("SETTINGS", "DATATYPE", "<null>", "YES", "INTEGER", "10"),
            column("SETTINGS", "POSITION", "<null>", "NO", "INTEGER", "10"),
            column("SOURCES", "ISLOCAL", "<null>", "NO", "CHAR", "1"),
            column("SOURCES", "NAME", "<null>", "YES", "VARCHAR", "255"),
            column("SOURCES", "UUID", "<null>", "NO", "VARCHAR", "255"),
            column("STATUSVALUES", "DISPLAYORDER", "<null>", "YES", "INTEGER", "10", true),
            column("STATUSVALUES", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("STATUSVALUES", "NAME", "<null>", "NO", "VARCHAR", "255"),
            column("STATUSVALUES", "RESERVED", "<null>", "NO", "CHAR", "1"),
            column("STATUSVALUESDES", "IDDES", "<null>", "NO", "INTEGER", "10"),
            column("STATUSVALUESDES", "LABEL", "<null>", "NO", "VARCHAR", "255"),
            column("STATUSVALUESDES", "LANGID", "<null>", "NO", "VARCHAR", "5"),
            column("THESAURUS", "ACTIVATED", "<null>", "NO", "CHAR", "1"),
            column("THESAURUS", "ID", "<null>", "NO", "VARCHAR", "255", true),
            column("USERGROUPS", "GROUPID", "<null>", "NO", "INTEGER", "10"),
            column("USERGROUPS", "PROFILE", "<null>", "NO", "INTEGER", "10"),
            column("USERGROUPS", "USERID", "<null>", "NO", "INTEGER", "10"),
            column("USERS", "AUTHTYPE", "<null>", "YES", "VARCHAR", "32"),
            column("USERS", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("USERS", "KIND", "<null>", "YES", "VARCHAR", "16"),
            column("USERS", "NAME", "<null>", "YES", "VARCHAR", "255"),
            column("USERS", "ORGANISATION", "<null>", "YES", "VARCHAR", "255"),
            column("USERS", "PASSWORD", "<null>", "NO", "VARCHAR", "120"),
            column("USERS", "PROFILE", "<null>", "NO", "INTEGER", "10"),
            column("USERS", "SECURITY", "<null>", "YES", "VARCHAR", "128"),
            column("USERS", "SURNAME", "<null>", "YES", "VARCHAR", "255"),
            column("USERS", "USERNAME", "<null>", "NO", "VARCHAR", "255"),
            column("ADDRESS", "ADDRESS", "<null>", "YES", "VARCHAR", "255"),
            column("ADDRESS", "CITY", "<null>", "YES", "VARCHAR", "255"),
            column("ADDRESS", "COUNTRY", "<null>", "YES", "VARCHAR", "255"),
            column("ADDRESS", "ID", "<null>", "NO", "INTEGER", "10", true),
            column("ADDRESS", "STATE", "<null>", "YES", "VARCHAR", "255"),
            column("ADDRESS", "ZIP", "<null>", "YES", "VARCHAR", "16"),
            column("USERADDRESS", "ADDRESSID", "<null>", "NO", "INTEGER", "10"),
            column("USERADDRESS", "USERID", "<null>", "NO", "INTEGER", "10"),
            column("EMAIL", "EMAIL", "<null>", "YES", "VARCHAR", "255"),
            column("EMAIL", "USER_ID", "<null>", "NO", "INTEGER", "10"),
            column("VALIDATION", "FAILED", "<null>", "NO", "INTEGER", "10"),
            column("VALIDATION", "METADATAID", "<null>", "NO", "INTEGER", "10"),
            column("VALIDATION", "STATUS", "<null>", "NO", "INTEGER", "10"),
            column("VALIDATION", "TESTED", "<null>", "NO", "INTEGER", "10"),
            column("VALIDATION", "VALDATE", "<null>", "YES", "VARCHAR", "30"),
            column("VALIDATION", "VALTYPE", "<null>", "NO", "VARCHAR", "40")
    };
}

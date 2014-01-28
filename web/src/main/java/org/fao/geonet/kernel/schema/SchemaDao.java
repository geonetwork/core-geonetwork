package org.fao.geonet.kernel.schema;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.SchematronCriteriaType;
import org.jdom.Element;

import javax.persistence.Transient;

/**
 * This class will probably dissappear with JPA
 * 
 * @author delawen
 * 
 */
public class SchemaDao {

	public static final String TABLE_SCHEMATRON = "schematron";
	public static final String TABLE_SCHEMATRON_CRITERIA = "schematroncriteria";

	public static final String COL_CRITERIA_ID = "id";
	public static final String COL_CRITERIA_SCHEMATRON_ID = "schematron";
	public static final String COL_CRITERIA_TYPE = "type";
	public static final String COL_CRITERIA_VALUE = "value";

	public static final String COL_SCHEMATRON_ID = "id";
	public static final String COL_SCHEMATRON_FILE = "file";
	public static final String COL_SCHEMATRON_ISO_SCHEMA = "isoschema";
	public static final String COL_SCHEMATRON_REQUIRED = "required";

	public static void insertSchematron(ServiceContext context, Dbms dbms,
			String file, String schemaName) throws SQLException {
		Integer id = null;
		if(context != null) {
			id = context.getSerialFactory().getSerial(dbms, TABLE_SCHEMATRON,
				COL_SCHEMATRON_ID);
		} else {
			//We must be on a testing outside jeeves environment
			Random r = new Random();
			id = r.nextInt();
		}
		dbms.execute("insert into " + TABLE_SCHEMATRON + " ("
				+ COL_SCHEMATRON_ID + "," + COL_SCHEMATRON_FILE + ","
				+ COL_SCHEMATRON_ISO_SCHEMA + "," + COL_SCHEMATRON_REQUIRED
				+ ") values (?,?,?,?)", id, file, schemaName, true);
	}

	public static void deleteCriteria(Dbms dbms, final Integer id)
			throws SQLException {
		dbms.execute("delete from " + TABLE_SCHEMATRON_CRITERIA + " where "
				+ COL_SCHEMATRON_ID + " = ?", id);
	}

	public static void insertCriteria(Dbms dbms, final Integer schematronId,
			final Integer id, final SchematronCriteriaType type,
			final String value) throws SQLException {
		dbms.execute("insert into " + TABLE_SCHEMATRON_CRITERIA + " ("
				+ COL_CRITERIA_ID + "," + COL_CRITERIA_SCHEMATRON_ID + ","
				+ COL_CRITERIA_TYPE + "," + COL_CRITERIA_VALUE
				+ ") values (?,?,?,?);", id, schematronId, type.ordinal(),
				value);
	}

	public static List<Element> selectCriteria(Dbms dbms, Integer id)
			throws SQLException {
		@SuppressWarnings("unchecked")
		List<Element> schematronCriteria = dbms.select(
				"select * from " + TABLE_SCHEMATRON_CRITERIA + " where "
						+ COL_CRITERIA_SCHEMATRON_ID + "=?", id).getChildren();
		return schematronCriteria;
	}

	public static List<Element> selectCriteriaBySchema(Dbms dbms, Integer id)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final List<Element> criterias = dbms.select(
				"SELECT DISTINCT " + COL_CRITERIA_TYPE + ", "
						+ COL_CRITERIA_VALUE + " FROM "
						+ TABLE_SCHEMATRON_CRITERIA + " WHERE "
						+ COL_CRITERIA_SCHEMATRON_ID + " = ? ", id)
				.getChildren();
		return criterias;
	}

	public static List<Element> selectSchemas(Dbms dbms) throws SQLException {
		@SuppressWarnings("unchecked")
		final List<Element> schematrons = dbms.select(
				"select * from " + TABLE_SCHEMATRON).getChildren();
		return schematrons;
	}

	public static List<Element> selectSchemas(Dbms dbms, String file)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final List<Element> schematrons = dbms.select(
				"select * from " + TABLE_SCHEMATRON + " where file=?", file)
				.getChildren();
		return schematrons;
	}

	/**
	 * Return the schematrons associated to a schema
	 * @param dbms
	 * @param schemaname
	 * @return
	 * @throws SQLException
	 */
	public static List<Element> selectSchema(Dbms dbms, String schemaname)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final List<Element> schematroncriteria = dbms.select(
				"SELECT DISTINCT " + COL_SCHEMATRON_ID + ", "
						+ COL_SCHEMATRON_FILE + ", " + COL_SCHEMATRON_REQUIRED
						+ " FROM " + TABLE_SCHEMATRON + " WHERE  "
						+ COL_SCHEMATRON_ISO_SCHEMA + " like ? ", schemaname)
				.getChildren();
		return schematroncriteria;
	}

    private final static int EXTENSION_LENGTH = ".xsl".length();
    private final static String SEPARATOR = File.separator;
    private final static String ALT_SEPARATOR;

    static {
        if (SEPARATOR.equals("\\")) {
            ALT_SEPARATOR = "/";
        } else {
            ALT_SEPARATOR = "\\";
        }
    }

    public static String toRuleName(String file) {
        if (file == null) {
            return "unnamed rule";
        }
        int lastSegmentIndex = file.lastIndexOf(SEPARATOR);
        if (lastSegmentIndex < 0) {
            lastSegmentIndex = file.lastIndexOf(ALT_SEPARATOR);
        }

        if (lastSegmentIndex < 0) {
            lastSegmentIndex = 0;
        } else {
            // drop the separator character
            lastSegmentIndex += 1;
        }

        String rule = file.substring(lastSegmentIndex, file.length() - EXTENSION_LENGTH);
        return rule ;
    }
}

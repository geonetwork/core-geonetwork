package org.fao.geonet.kernel.schema;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

import org.jdom.Element;

/**
 * This class will probably dissappear with JPA
 * 
 * @author delawen
 * 
 */
public class SchemaDao {

	public static final String TABLE_SCHEMATRON = "schematron";
	public static final String TABLE_SCHEMATRON_CRITERIA = "schematroncriteria";
	public static final String TABLE_SCHEMATRON_CRITERIA_GROUP = "schematroncriteriagroup";

	public static final String COL_CRITERIA_ID = "id";
	public static final String COL_CRITERIA_GROUP_NAME = "group_name";
	public static final String COL_CRITERIA_TYPE = "type";
	public static final String COL_CRITERIA_VALUE = "value";

	public static final String COL_SCHEMATRON_ID = "id";
	public static final String COL_SCHEMATRON_FILE = "file";
	public static final String COL_SCHEMATRON_SCHEMA_NAME = "schemaname";

	public static final String COL_GROUP_NAME= "name";
	public static final String COL_GROUP_REQUIREMENT = "requirement";
	public static final String COL_GROUP_SCHEMATRON_ID = "schematron";

	public static Integer insertSchematron(ServiceContext context, Dbms dbms,
                                           String file, String schemaName) throws SQLException {
		Integer id;
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
				+ COL_SCHEMATRON_SCHEMA_NAME
				+ ") values (?,?,?)", id, file, schemaName);

        return id;
	}

	public static void deleteCriteria(Dbms dbms, final Integer id)
			throws SQLException {
		dbms.execute("delete from " + TABLE_SCHEMATRON_CRITERIA + " where "
				+ COL_SCHEMATRON_ID + " = ?", id);
	}

	public static void insertCriteria(Dbms dbms, final String groupName,
			final Integer id, final SchematronCriteriaType type,
			final String value) throws SQLException {
		dbms.execute("insert into " + TABLE_SCHEMATRON_CRITERIA + " ("
				+ COL_CRITERIA_ID + "," + COL_CRITERIA_GROUP_NAME + ","
				+ COL_CRITERIA_TYPE + "," + COL_CRITERIA_VALUE
				+ ") values (?,?,?,?);", id, groupName, type.name(),
				value);
	}

	public static List<SchematronCriteriaGroup> selectCriteriaBySchema(final Dbms dbms, Integer schematronId)
			throws SQLException {
        final String FIND_GROUP_QUERY = "select * FROM  SchematronCriteriaGroup schematronGroup WHERE schematronGroup.schematron=?";
        final String LOAD_GROUP_QUERY = "select \n"
                                        + "    criteria.id, \n"
                                        + "    criteria.type, \n"
                                        + "    criteria.value\n"
                                        + "from \n"
                                        + "    SchematronCriteriaGroup schematronGroup \n"
                                        + "left outer join \n"
                                        + "    SchematronCriteria criteria on schematronGroup.name=criteria.group_name \n"
                                        + "where \n"
                                        + "    schematronGroup.name=?\n";

        @SuppressWarnings("unchecked")
        List<Element> groupNames = dbms.select(FIND_GROUP_QUERY, schematronId).getChildren();

        return Lists.transform(groupNames , new Function<Element, SchematronCriteriaGroup>() {
            @Override
            public SchematronCriteriaGroup apply(Element input) {
                try {
                    SchematronCriteriaGroup group = new SchematronCriteriaGroup();
                    List<Element> criteriaResult = dbms.select(LOAD_GROUP_QUERY, input.getChildTextTrim(COL_GROUP_NAME)).getChildren();
                    for (Element criteriaElement : criteriaResult) {
                        SchematronCriteria criteria = new SchematronCriteria();

                        criteria.setType(SchematronCriteriaType.valueOf(criteriaElement.getChildText(COL_CRITERIA_TYPE)));
                        criteria.setValue(criteriaElement.getChildText(COL_CRITERIA_VALUE));

                        group.getCriteriaList().add(criteria);

                        group.setName(input.getChildText(COL_GROUP_NAME));
                        group.setRequirement(SchematronRequirement.valueOf(input.getChildText(COL_GROUP_REQUIREMENT)));
                        group.setSchematronId(Integer.parseInt(input.getChildText(COL_GROUP_SCHEMATRON_ID)));
                    }

                    return group;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
	 * 
	 * Returns if the schematron is mandatory
	 * 
	 * @param dbms
	 * @param schematronId
	 * @return
	 * @throws SQLException
	 */
	public static Element isRequired(Dbms dbms, Integer schematronId)
			throws SQLException {
		@SuppressWarnings("unchecked")
		final List<Element> schematrons = dbms.select(
				"select distinct required from " + TABLE_SCHEMATRON + " where id=? limit 1", schematronId)
				.getChildren();
		
		if(schematrons.isEmpty())
			return null;
	
		return schematrons.get(0);
	}	
	
	/**
	 * Toggle the mandatory field of the schematron
	 * @param dbms
	 * @param schematronId
	 * @return
	 * @throws SQLException
	 */
	public static void toggleRequired(Dbms dbms, Integer schematronId)
			throws SQLException {
		dbms.execute(
				"update " + TABLE_SCHEMATRON + " set required = not(required) where id=?", schematronId);
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
						+ COL_SCHEMATRON_FILE
						+ " FROM " + TABLE_SCHEMATRON + " WHERE  "
						+ COL_SCHEMATRON_SCHEMA_NAME + " like ? ", schemaname)
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

    public static SchematronCriteriaGroup selectCriteriaGroup(Dbms dbms, String groupName) throws SQLException {
        @SuppressWarnings("unchecked")
        List<Element> select = dbms.select("SELECT * from " + TABLE_SCHEMATRON_CRITERIA_GROUP + " WHERE name = ?", groupName).getChildren();

        if (select.isEmpty()) {
            return null;
        } else {
            Element el = select.get(0);
            return new SchematronCriteriaGroup()
                    .setName(el.getChildText(COL_GROUP_NAME))
                    .setRequirement(SchematronRequirement.valueOf(el.getChildText(COL_GROUP_REQUIREMENT)))
                    .setSchematronId(Integer.parseInt(el.getChildText(COL_GROUP_SCHEMATRON_ID)));
        }
    }

    public static void insertGroup(ServiceContext context, Dbms dbms, SchematronCriteriaGroup criteriaGroup) throws SQLException {
        dbms.execute("INSERT INTO "+TABLE_SCHEMATRON_CRITERIA_GROUP+" ("+COL_GROUP_NAME+","+COL_GROUP_REQUIREMENT+","+COL_GROUP_SCHEMATRON_ID+") VALUES (?,?,?)",
                criteriaGroup.getName(), criteriaGroup.getRequirement().name(), criteriaGroup.getSchematronId());
        for (SchematronCriteria criteria : criteriaGroup.getCriteriaList()) {
            Integer id;
            if(context != null) {
                id = context.getSerialFactory().getSerial(dbms, TABLE_SCHEMATRON_CRITERIA,
                        COL_CRITERIA_ID);
            } else {
                //We must be on a testing outside jeeves environment
                Random r = new Random();
                id = r.nextInt();
            }

            insertCriteria(dbms, criteriaGroup.getName(), id, criteria.getType(), criteria.getValue());
        }
    }
}

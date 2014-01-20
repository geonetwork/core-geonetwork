package org.fao.geonet.kernel.schema;

import java.sql.SQLException;

import jeeves.resources.dbms.Dbms;

import org.fao.geonet.domain.SchematronCriteriaType;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Test;

/**
 * Based on XmlSerializerTest on the db mock
 * */
public class SchemaDaoTest {

	private static final String JDBC_H2_TEST = "jdbc:h2:˜/test";
	private static final String SA = "sa";
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

	private Dbms dbms = mockDbms();

	@Test
	public void test() throws SQLException {

		Integer schematron = 1;

		SchemaDao.insertSchematron(null, dbms, "file", "name");

		Assert.assertEquals(1, SchemaDao.selectSchemas(dbms).size());
		Assert.assertEquals(1, SchemaDao.selectSchemas(dbms, "file").size());

		Assert.assertEquals(0,
				SchemaDao.selectCriteriaBySchema(dbms, schematron).size());

		int max = 5;
		for (int id = 1; id < max; id++) {
			SchemaDao.insertCriteria(dbms, schematron, id,
					SchematronCriteriaType.GROUP, Integer.toString(id));
			Assert.assertEquals(id, SchemaDao.selectCriteria(dbms, schematron).size());
		}

		Assert.assertEquals(max - 1,
				SchemaDao.selectCriteriaBySchema(dbms, schematron).size());

		for (Integer id = 1; id < max; id++) {
			SchemaDao.deleteCriteria(dbms, id);
		}
		Assert.assertEquals(0,
				SchemaDao.selectCriteriaBySchema(dbms, schematron).size());

	}

	private Dbms mockDbms() {

		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(JDBC_H2_TEST);
		ds.setUser(SA);
		ds.setPassword(SA);

		Dbms dbms = null;
		try {
			dbms = new Dbms(ds, JDBC_H2_TEST);

			dbms.connect(SA, SA);

			dbms.execute("DROP TABLE schematron;");
			dbms.execute("DROP TABLE schematroncriteria;");
			dbms.execute("DROP TABLE schematrondes;");

			dbms.execute("CREATE TABLE IF NOT EXISTS schematron (id integer NOT NULL,"
					+ "  file character varying(255) NOT NULL,"
					+ "    isoschema character varying(255) NOT NULL,"
					+ "    required boolean NOT NULL,"
					+ "    CONSTRAINT schematron_pkey PRIMARY KEY (id)"
					+ "  );");

			dbms.execute("CREATE TABLE IF NOT EXISTS schematroncriteria"
					+ "  ("
					+ "    id integer NOT NULL,"
					+ "    type integer NOT NULL,"
					+ "    value character varying(255) NOT NULL,"
					+ "    schematron integer NOT NULL,"
					+ "    CONSTRAINT schematroncriteria_pkey PRIMARY KEY (id),"
					+ "  );");

			dbms.execute("CREATE TABLE IF NOT EXISTS schematrondes" + "  ("
					+ "     iddes integer NOT NULL,"
					+ "     label character varying(96) NOT NULL,"
					+ "     langid character varying(5) NOT NULL,  );");

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		return dbms;
	}
}

package org.fao.geonet.kernel.schema;

import java.sql.SQLException;

import jeeves.resources.dbms.Dbms;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Based on XmlSerializerTest on the db mock
 * */
public class SchemaDaoTest {

	private static final String JDBC_H2_TEST = "jdbc:h2:schematrontest";
	private static final String SA = "sa";

	private static Dbms dbms;

	@Before
	public void cleanDB() throws Exception {

		try {
			dbms.execute("DELETE FROM schematron;");
			dbms.execute("DELETE FROM schematroncriteria;");
			dbms.execute("DELETE FROM schematrondes;");

			dbms.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@BeforeClass
	public static void mockDbms() {

		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(JDBC_H2_TEST);
		ds.setUser(SA);
		ds.setPassword(SA);

		try {
			dbms = new Dbms(ds, JDBC_H2_TEST);

			dbms.connect(SA, SA);

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
	}
}

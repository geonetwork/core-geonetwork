package org.fao.geonet;

import java.sql.SQLException;

import jeeves.resources.dbms.Dbms;

/**
 * A task for migrating the database from one version to another
 * 
 * @author jeichar
 */
public interface DatabaseMigrationTask {
	void update(Dbms dbms) throws SQLException;
}

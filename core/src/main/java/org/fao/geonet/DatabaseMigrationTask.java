package org.fao.geonet;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.kernel.setting.SettingManager;

/**
 * A task for migrating the database from one version to another
 * 
 * @author jeichar
 */
public interface DatabaseMigrationTask {
	void update(Statement statement) throws SQLException;
}

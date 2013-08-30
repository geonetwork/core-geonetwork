package org.fao.geonet;

import java.sql.SQLException;

import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.kernel.setting.SettingManager;

import jeeves.resources.dbms.Dbms;

/**
 * A task for migrating the database from one version to another
 * 
 * @author jeichar
 */
public interface DatabaseMigrationTask {
	void update(SettingManager settings, HarvesterSettingsManager harvesterSettingsMan, Dbms dbms) throws SQLException;
}

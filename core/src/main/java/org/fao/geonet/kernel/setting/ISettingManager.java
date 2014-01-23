package org.fao.geonet.kernel.setting;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.Setting;
import org.jdom.Element;

/**
 * Used to change from Jeeves to Spring MVC easily. Deprecating all Element
 * objects, as without Jeeves it doesn't make sense.
 * 
 * @author delawen
 * 
 */
public interface ISettingManager {

	/**
	 * Get all settings as xml.
	 * 
	 * @param asTree
	 *            get the settings as a tree
	 * 
	 * @return all settings as xml.
	 */
	@Deprecated
	public abstract Element getAllAsXML(boolean asTree);

	/**
	 * Get all settings as list.
	 * 
	 * 
	 * @return all settings as list.
	 */
	public abstract List<Setting> getAllAsList();

	/**
	 * Return a setting by its key
	 * 
	 * @param path
	 *            eg. system/site/name
	 */
	public abstract String getValue(String path);

	/**
	 * Return a set of values as XML
	 * 
	 * @param keys
	 *            A list of setting's key to retrieve
	 */
	@Deprecated
	public abstract Element getValues(String[] keys);

	/**
	 * Get value of a setting as boolean
	 * 
	 * @param key
	 *            The setting key
	 * @return The setting valueThe setting key
	 */
	public abstract boolean getValueAsBool(String key);

	/**
	 * Get value of a setting as boolean
	 * 
	 * @param key
	 *            The setting key
	 * @param defaultValue
	 *            The default value
	 * @return The setting value as boolean
	 */
	public abstract boolean getValueAsBool(String key, boolean defaultValue);

	/**
	 * Get value of a setting as integer
	 * 
	 * @param key
	 *            The setting key
	 * @return The integer value of the setting or null
	 */
	public abstract Integer getValueAsInt(String key);

	/**
	 * Set the value of a Setting entity
	 * 
	 * @param key
	 *            the path/name/key of the setting.
	 * @param value
	 *            the new value
	 * 
	 * @return true if the types are correct and the setting is found.
	 */
	public abstract boolean setValue(String key, String value);

	/**
	 * Set the setting value by key to the boolean value.
	 * 
	 * @param key
	 *            the key/path/name of the setting.
	 * @param value
	 *            the new boolean value
	 */
	public abstract boolean setValue(String key, boolean value);

	/**
	 * Set a list of settings.
	 * 
	 * @param values
	 *            The settings to update
	 * @return true if the types are correct and the setting is found.
	 * 
	 * @throws SQLException
	 */
	public abstract boolean setValues(Map<String, String> values);

	/**
	 * Refreshes current settings manager. This has to be used when updating the
	 * Settings table without using this class. For example when using an SQL
	 * script.
	 */
	public abstract boolean refresh() throws SQLException;

	public abstract String getSiteId();

	public abstract String getSiteName();

	public abstract void setSiteUuid(String siteUuid);

	public abstract String getSiteURL(ServiceContext context);

	public abstract boolean getHideWitheldElements();

	public abstract boolean setHideWitheldElements(boolean value);

}
package org.fao.geonet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import jeeves.server.ServiceConfig;
import jeeves.server.sources.http.JeevesServlet;
import jeeves.utils.BinaryFile;
import jeeves.utils.Log;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;

/**
 * The GeoNetwork data directory is the location on the file system where
 * GeoNetwork stores all of its custom configuration. This configuration defines
 * such things as: What thesaurus is used by GeoNetwork? What schema is plugged
 * in GeoNetwork?. The data directory also contains a number of support files
 * used by GeoNetwork for various purposes (eg. Lucene index, spatial index,
 * logos).
 */
public class GeonetworkDataDirectory {
	/**
	 * The default GeoNetwork data directory location.
	 */
	private static final String GEONETWORK_DEFAULT_DATA_DIR = "WEB-INF" + File.separator + "data"  + File.separator;
	public static final String KEY_SUFFIX = ".dir";
	public static final String GEONETWORK_DIR_KEY = "geonetwork.dir";

	private String systemDataDir;
	private JeevesServlet jeevesServlet;

	/**
	 * Check and create if needed GeoNetwork data directory.
	 * 
	 * The data directory is the only mandatory value. If not set, the default location is
	 * {@link #GEONETWORK_DEFAULT_DATA_DIR}.
	 * 
	 * All properties are set using :
	 * <ul>
	 * <ol>
	 * Java environment variable
	 * </ol>
	 * <ol>
	 * Servlet context parameter
	 * </ol>
	 * <ol>
	 * System environment variable
	 * </ol>
	 * </ul>
	 * 
	 */
	public GeonetworkDataDirectory(String webappName, String path,
			ServiceConfig handlerConfig, JeevesServlet jeevesServlet) {
        if (Log.isDebugEnabled(Geonet.DATA_DIRECTORY))
            Log.debug(Geonet.DATA_DIRECTORY,
				"Check and create if needed GeoNetwork data directory");
		this.jeevesServlet = jeevesServlet;
		setDataDirectory(webappName, path, handlerConfig);
	}

	/**
	 * Determines the location of a property based on the
	 * following lookup mechanism:
	 * 
	 * 1) Java environment variable 2) Servlet context variable 3) Config.xml appHandler parameter 4) System
	 * variable
	 * 
	 * For each of these, the methods checks that 1) The path exists 2) Is a
	 * directory 3) Is writable
	 * 
	 * Inspired by GeoServer mechanism.
	 * @param handlerConfig TODO
	 * @param servContext
	 *            The servlet context.
	 * 
	 * @return String The absolute path to the data directory, or
	 *         <code>null</code> if it could not be found.
	 */
	private static String lookupProperty(JeevesServlet jeevesServlet, ServiceConfig handlerConfig, String key) {

		final String[] typeStrs = { "Java environment variable ",
				"Servlet context parameter ", "Config.xml appHandler parameter", "System environment variable " };

		String dataDirStr = null;

        if (Log.isDebugEnabled(Geonet.DATA_DIRECTORY))
            Log.debug(Geonet.DATA_DIRECTORY, "lookupProperty " + key);
		
		// Loop over variable access methods
		for (int j = 0; j < typeStrs.length && dataDirStr == null; j++) {
			String value = null;
			String typeStr = typeStrs[j];

			// Lookup section
			switch (j) {
			case 0:
				value = System.getProperty(key);
				break;
			case 1:
				value = jeevesServlet == null ? null : jeevesServlet.getInitParameter(key);
				break;
			case 2:
				value = handlerConfig.getValue(key);
				break;
			case 3:
//				Environment variable names used by the utilities in the Shell and Utilities 
//				volume of IEEE Std 1003.1-2001 consist solely of uppercase letters, digits, and the '_' 
//				Instead of looking for geonetwork.dir, get geonetwork_dir
				value = System.getenv(key.replace('.', '_'));
				break;
			default:
			    throw new IllegalArgumentException("Did not expect value: "+j);
			}

			if (value == null || value.equalsIgnoreCase("")) {
				continue;
			}
            if (Log.isDebugEnabled(Geonet.DATA_DIRECTORY))
                Log.debug(Geonet.DATA_DIRECTORY, " Found " + typeStr + "for " + key
					+ " with value " + value);
			
			dataDirStr = value;
		}

		return dataDirStr;
	}

	/**
	 * 
	 */
	private String setDataDirectory(String webappName, String path,
			ServiceConfig handlerConfig) {

		// System property defined according to webapp name
		systemDataDir = GeonetworkDataDirectory.lookupProperty(jeevesServlet,
				handlerConfig, webappName + KEY_SUFFIX);

		// GEONETWORK.dir is default
		if (systemDataDir == null) {
			systemDataDir = GeonetworkDataDirectory.lookupProperty(
					jeevesServlet, handlerConfig, GEONETWORK_DIR_KEY);
		}
		boolean useDefaultDataDir = false;
		Log.warning(Geonet.DATA_DIRECTORY,
				"   - Data directory initialization: " + systemDataDir);
		
		File systemDataFolder;
		
		if (systemDataDir == null) {
			Log.warning(Geonet.DATA_DIRECTORY,
					"    - Data directory properties is not set. Use "
							+ webappName + KEY_SUFFIX + " or " + GEONETWORK_DIR_KEY
							+ " properties.");
			useDefaultDataDir = true;
		} else {
			systemDataFolder = new File(systemDataDir);
			if (!systemDataFolder.exists()) {
				Log.warning(Geonet.DATA_DIRECTORY,
						"    - Data directory does not exist. Create it first.");
				useDefaultDataDir = true;
			}
	
			if (!systemDataFolder.canWrite()) {
				Log.warning(
						Geonet.DATA_DIRECTORY,
						"    - Data directory is not writable. Set read/write privileges to user starting the catalogue (ie. "
								+ System.getProperty("user.name") + ").");
				useDefaultDataDir = true;
			}
	
			if (!systemDataFolder.isAbsolute()) {
				Log.warning(
						Geonet.DATA_DIRECTORY,
						"    - Data directory is not an absolute path. Relative path is not recommended.\n"
								+ "Update "
								+ webappName
								+ KEY_SUFFIX + " or geonetwork.dir environment variable.");
				useDefaultDataDir = true;
			}
		}
		
		if (useDefaultDataDir) {
			systemDataDir = path + GEONETWORK_DEFAULT_DATA_DIR;
			Log.warning(Geonet.DATA_DIRECTORY,
					"    - Data directory provided could not be used. Using default location: "
							+ systemDataDir);
		}

		if (!systemDataDir.endsWith(File.separator)) {
			systemDataDir += File.separator;
		}

		Log.info(Geonet.DATA_DIRECTORY, "   - Data directory is: "
				+ systemDataDir);
		System.setProperty(webappName + KEY_SUFFIX + "", systemDataDir);

		// Set subfolder data directory
		setResourceDir(webappName, handlerConfig, systemDataDir, ".lucene" + KEY_SUFFIX,
				"index", Geonet.Config.LUCENE_DIR);
		setResourceDir(webappName, handlerConfig, systemDataDir, ".config" + KEY_SUFFIX,
				"config", Geonet.Config.CONFIG_DIR);
		setResourceDir(webappName, handlerConfig, systemDataDir,
				".codeList" + KEY_SUFFIX, "config" + File.separator + "codelist",
				Geonet.Config.CODELIST_DIR);
		setResourceDir(webappName, handlerConfig, systemDataDir, ".schema" + KEY_SUFFIX,
				"config" + File.separator + "schema_plugins",
				Geonet.Config.SCHEMAPLUGINS_DIR);
		setResourceDir(webappName, handlerConfig, systemDataDir, ".data" + KEY_SUFFIX,
				"data" + File.separator + "metadata_data",
				Geonet.Config.DATA_DIR);
		setResourceDir(webappName, handlerConfig, systemDataDir, ".svn" + KEY_SUFFIX,
				"data" + File.separator + "metadata_subversion",
				Geonet.Config.SUBVERSION_PATH);
		setResourceDir(webappName, handlerConfig, systemDataDir,
				".resources" + KEY_SUFFIX, "data" + File.separator + "resources",
				Geonet.Config.RESOURCES_DIR);

		handlerConfig.setValue(Geonet.Config.HTMLCACHE_DIR,
				handlerConfig.getValue(Geonet.Config.RESOURCES_DIR)
						+ File.separator + "htmlcache");

		handlerConfig.setValue(Geonet.Config.SYSTEM_DATA_DIR, systemDataDir);

		initDataDirectory(path, handlerConfig);

		return systemDataDir;
	}

	/**
	 * Checks if data directory is empty or not. If empty, add mandatory
	 * elements (ie. codelist).
	 * 
	 * @param dataSystemDir
	 * @param path
	 */
	private void initDataDirectory(String path, ServiceConfig handlerConfig) {
		Log.info(Geonet.DATA_DIRECTORY,
				"   - Data directory initialization ...");

		File codelistDir = new File(
				handlerConfig.getValue(Geonet.Config.CODELIST_DIR));
		if (!codelistDir.exists()) {
			Log.info(Geonet.DATA_DIRECTORY,
					"     - Copying codelists directory ...");
			try {
				BinaryFile.copyDirectory(new File(path
						+ GEONETWORK_DEFAULT_DATA_DIR + "codelist"),
						codelistDir);
			} catch (IOException e) {
				Log.info(Geonet.DATA_DIRECTORY,
						"     - Copy failed: " + e.getMessage());
				e.printStackTrace();
			}
		}

		String schemaCatPath = handlerConfig.getValue(Geonet.Config.CONFIG_DIR)
				+ File.separator + Geonet.File.SCHEMA_PLUGINS_CATALOG;
		File schemaCatFile = new File(schemaCatPath);
		if (!schemaCatFile.exists()) {
			Log.info(Geonet.DATA_DIRECTORY,
					"     - Copying schema plugin catalogue ...");
			FileInputStream in = null;
			FileOutputStream out = null;
			try {
                in = new FileInputStream(path + "WEB-INF"
						+ File.separator + Geonet.File.SCHEMA_PLUGINS_CATALOG);
                out = new FileOutputStream(schemaCatFile);

				BinaryFile.copy(in, out);
			} catch (IOException e) {
				Log.info(
						Geonet.DATA_DIRECTORY,
						"      - Error copying schema plugin catalogue: "
								+ e.getMessage());
			} finally {
		        IOUtils.closeQuietly(in);
		        IOUtils.closeQuietly(out);
			}
		}

	}

	/**
	 * Try to retrieve from system properties the variable with name
	 * <webapp.name>.key. If not set, create the resource folder using
	 * <geonetwork.dir>/folder and set the system property value. Create the
	 * folder if does not exist.
	 * 
	 * @param webappName
	 * @param handlerConfig
	 * @param systemDataDir
	 * @param key
	 * @param folder
	 * @param handlerKey
	 */
	private void setResourceDir(String webappName, ServiceConfig handlerConfig,
			String systemDataDir, String key, String folder, String handlerKey) {
		String envKey = webappName + key;
		String dir = GeonetworkDataDirectory.lookupProperty(
				jeevesServlet, handlerConfig, envKey);
		if (dir == null) {
			dir = systemDataDir + folder;
			System.setProperty(envKey, dir);
		} else {
			if (!new File(dir).isAbsolute()) {
				Log.info(Geonet.DATA_DIRECTORY, "    - " + envKey
						+ " for directory " + dir
						+ " is relative path. Use absolute path instead.");
			}
		}
		handlerConfig.setValue(handlerKey, dir);

		// Create directory if it does not exist
		File file = new File(dir);
		if (!file.exists() && !file.mkdirs()) {
			throw new RuntimeException("Unable to create directory: "+file);
		}

		Log.info(Geonet.DATA_DIRECTORY, "    - " + envKey + " is " + dir);
	}

}

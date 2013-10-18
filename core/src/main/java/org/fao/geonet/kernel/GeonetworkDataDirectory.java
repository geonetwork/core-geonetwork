package org.fao.geonet.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import jeeves.server.ServiceConfig;
import jeeves.server.sources.http.JeevesServlet;
import jeeves.server.sources.http.ServletPathFinder;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * The GeoNetwork data directory is the location on the file system where
 * GeoNetwork stores all of its custom configuration. This configuration defines
 * such things as: What thesaurus is used by GeoNetwork? What schema is plugged
 * in GeoNetwork?. The data directory also contains a number of support files
 * used by GeoNetwork for various purposes (eg. Lucene index, spatial index,
 * logos).
 */
@Component(GeonetworkDataDirectory.GEONETWORK_BEAN_KEY)
public class GeonetworkDataDirectory {
	/**
	 * The default GeoNetwork data directory location.
	 */
	private static final String GEONETWORK_DEFAULT_DATA_DIR = "WEB-INF" + File.separator + "data"  + File.separator;
	public static final String KEY_SUFFIX = ".dir";
	public static final String GEONETWORK_DIR_KEY = "geonetwork.dir";
	public static final String GEONETWORK_BEAN_KEY = GEONETWORK_DIR_KEY+".spring.bean";

    private String webappDir;
    private String systemDataDir;
    private File luceneDir;
    private File spatialIndexPath;
    private File configDir;
    private File thesauriDir;
    private File schemaPluginsDir;
    private File metadataDataDir;
    private File metadataRevisionDir;
    private File resourcesDir;
    private File htmlCacheDir;

    @Autowired
    private ConfigurableApplicationContext _applicationContext;

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
	public void init(final String webappName, final String path,
                     final ServiceConfig handlerConfig, final JeevesServlet jeevesServlet) {
        if (Log.isDebugEnabled(Geonet.DATA_DIRECTORY))
            Log.debug(Geonet.DATA_DIRECTORY,
				"Check and create if needed GeoNetwork data directory");
        this.webappDir = path;
		setDataDirectory(jeevesServlet, webappName, path, handlerConfig);
	}
	public void init(final String webappName, final String path,  String systemDataDir,
                     final ServiceConfig handlerConfig, final JeevesServlet jeevesServlet) {
        this.systemDataDir = systemDataDir;
        this.init(webappName, path, handlerConfig, jeevesServlet);
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

	private String setDataDirectory(JeevesServlet jeevesServlet, String webappName, String path,
                                    ServiceConfig handlerConfig) {

        if (systemDataDir == null) {
            // System property defined according to webapp name
            systemDataDir = GeonetworkDataDirectory.lookupProperty(jeevesServlet,
                    handlerConfig, webappName + KEY_SUFFIX);
        }
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
		luceneDir = setDir(jeevesServlet, webappName, handlerConfig, systemDataDir, ".lucene" + KEY_SUFFIX,
                "index", Geonet.Config.LUCENE_DIR);
		spatialIndexPath = setDir(jeevesServlet, "", handlerConfig, systemDataDir, "spatial" + KEY_SUFFIX,
                "spatialindex", null);

		configDir = setDir(jeevesServlet, webappName, handlerConfig, systemDataDir, ".config" + KEY_SUFFIX,
                "config", Geonet.Config.CONFIG_DIR);
		thesauriDir = setDir(jeevesServlet, webappName, handlerConfig, systemDataDir,
                ".codeList" + KEY_SUFFIX, "config" + File.separator + "codelist",
                Geonet.Config.CODELIST_DIR);
		schemaPluginsDir = setDir(jeevesServlet, webappName, handlerConfig, systemDataDir, ".schema" + KEY_SUFFIX,
                "config" + File.separator + "schema_plugins",
                Geonet.Config.SCHEMAPLUGINS_DIR);
		metadataDataDir = setDir(jeevesServlet, webappName, handlerConfig, systemDataDir, ".data" + KEY_SUFFIX,
                "data" + File.separator + "metadata_data",
                Geonet.Config.DATA_DIR);
		metadataRevisionDir = setDir(jeevesServlet, webappName, handlerConfig, systemDataDir, ".svn" + KEY_SUFFIX,
                "data" + File.separator + "metadata_subversion",
                Geonet.Config.SUBVERSION_PATH);
		resourcesDir = setDir(jeevesServlet, webappName, handlerConfig, systemDataDir,
                ".resources" + KEY_SUFFIX, "data" + File.separator + "resources",
                Geonet.Config.RESOURCES_DIR);

        htmlCacheDir = new File(handlerConfig.getValue(Geonet.Config.RESOURCES_DIR), "htmlcache");
        handlerConfig.setValue(Geonet.Config.HTMLCACHE_DIR, htmlCacheDir.getAbsolutePath());
        try {
            IO.mkdirs(htmlCacheDir, "HTML Cache Directory");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        handlerConfig.setValue(Geonet.Config.SYSTEM_DATA_DIR, systemDataDir);

		initDataDirectory(path, handlerConfig);

		return systemDataDir;
	}

	/**
	 * Checks if data directory is empty or not. If empty, add mandatory
	 * elements (ie. codelist).
	 * 
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
	 *
     * @param jeevesServlet
     * @param webappName
     * @param handlerConfig
     * @param systemDataDir
     * @param key
     * @param folder
     * @param handlerKey       @return
     * */
	private File setDir(JeevesServlet jeevesServlet, String webappName,
                        ServiceConfig handlerConfig, String systemDataDir, String key, String folder, String handlerKey) {
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
		if(handlerKey != null) {
		    handlerConfig.setValue(handlerKey, dir);
		}

		// Create directory if it does not exist
		File file = new File(dir);
		if (!file.exists() && !file.mkdirs()) {
			throw new RuntimeException("Unable to create directory: " + file);
		}

        System.setProperty(envKey, dir);
		Log.info(Geonet.DATA_DIRECTORY, "    - " + envKey + " is " + dir);
		return file;
	}

    /**
     * Get the root data dir for Geonetwork.  Typically other "data" directories are subdirectories to this.
     *
     * @return the root data dir for Geonetwork
     */
    public String getSystemDataDir() {
        return systemDataDir;
    }
    /**
     * Set the root data dir for Geonetwork.  Typically other "data" directories are subdirectories to this.
     */
    public void setSystemDataDir(String systemDataDir) {
        this.systemDataDir = systemDataDir;
    }
    /**
     * Get the directory to store the lucene indices in.
     * @return The directory to store the lucene indices in.
     */
    public File getLuceneDir() {
        return luceneDir;
    }
    /**
     * Set the directory to store the lucene indices in.
     */
    public void setLuceneDir(File luceneDir) {
        this.luceneDir = luceneDir;
    }

    /**
     * Get the directory to store the metadata spatial index. If the spatial index is to be stored locally this is the directory to use.
     *
     * @return the directory to store the metadata spatial index
     */
    public File getSpatialIndexPath() {
        return spatialIndexPath;
    }
    /**
     * Set the directory to store the metadata spatial index. If the spatial index is to be stored locally this is the directory to use.
     */
    public void setSpatialIndexPath(File spatialIndexPath) {
        this.spatialIndexPath = spatialIndexPath;
    }

    /**
     * Return the directory containing the configuration file.
     *
     * @return the directory containing the configuration file.
     */
    public File getConfigDir() {
        return configDir;
    }
    /**
     * Set the directory containing the configuration file.
     */
    public void setConfigDir(File configDir) {
        this.configDir = configDir;
    }
    /**
     * Get the directory containing the thesauri.
     *
     * @return the directory containing the thesauri.
     */
    public File getThesauriDir() {
        return thesauriDir;
    }
    /**
     * Set the directory containing the thesauri.
     */
    public void setThesauriDir(File thesauriDir) {
        this.thesauriDir = thesauriDir;
    }

    /**
     * Get the schema plugins directory.
     *
     * @return the schema plugins directory.
     */
    public File getSchemaPluginsDir() {
        return schemaPluginsDir;
    }
    /**
     * Set the schema plugins directory.
     */
    public void setSchemaPluginsDir(File schemaPluginsDir) {
        this.schemaPluginsDir = schemaPluginsDir;
    }
    /**
     * Get the directory that contain all the resources related to metadata (thumbnails, attachments, etc...).
     *
     * @return the directory that contain all the resources related to metadata (thumbnails, attachments, etc...).
     */
    public File getMetadataDataDir() {
        return metadataDataDir;
    }
    /**
     * Set the directory that contain all the resources related to metadata (thumbnails, attachments, etc...).
     */
    public void setMetadataDataDir(File metadataDataDir) {
        this.metadataDataDir = metadataDataDir;
    }
    /**
     * Get the directory containing the metadata revision history if it is to be stored locally.
     * @return the directory containing the metadata revision history.
     */
    public File getMetadataRevisionDir() {
        return metadataRevisionDir;
    }
    /**
     * Set the directory containing the metadata revision history if it is to be stored locally.
     */
    public void setMetadataRevisionDir(File metadataRevisionDir) {
        this.metadataRevisionDir = metadataRevisionDir;
    }
    /**
     * Get the directory that will contain the resources for the system.
     *
     * @see org.fao.geonet.resources.ResourceFilter
     * @return the directory that will contain the resources for the system.
     */
    public File getResourcesDir() {
        return resourcesDir;
    }
    /**
     * Set the directory that will contain the resources for the system.
     */
    public void setResourcesDir(File resourcesDir) {
        this.resourcesDir = resourcesDir;
    }

    /**
     * Get the directory containing the webapplication.
     *
     * @return the directory containing the webapplication.
     */
    public String getWebappDir() {
        return webappDir;
    }

    /**
     * Get directory for caching html data.
     *
     * @return directory for caching html data.
     */
    public File getHtmlCacheDir() {
        return htmlCacheDir;
    }
    /**
     * Set directory for caching html data.
     */
    public void setHtmlCacheDir(File htmlCacheDir) {
        this.htmlCacheDir = htmlCacheDir;
    }



}

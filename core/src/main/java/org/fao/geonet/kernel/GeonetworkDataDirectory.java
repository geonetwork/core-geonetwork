package org.fao.geonet.kernel;

import jeeves.server.ServiceConfig;
import jeeves.server.sources.http.JeevesServlet;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

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
//    static final String GEONETWORK_DEFAULT_DATA_DIR = Joiner.on("/").join(GEONETWORK_DEFAULT_DATA_DIR_PARTS);
    /**
     * A suffix of the keys used to look up paths in system.properties or system.env or in Servlet context.
     */
    public static final String KEY_SUFFIX = ".dir";
    /**
     * The full key of the geonetwork data directory.
     */
	public static final String GEONETWORK_DIR_KEY = "geonetwork.dir";
    /**
     * The id used when registering this object in a spring application context.
     */
	public static final String GEONETWORK_BEAN_KEY = "GeonetworkDataDirectory";

    private Path webappDir;
    private Path systemDataDir;
    private Path luceneDir;
    private Path spatialIndexPath;
    private Path configDir;
    private Path thesauriDir;
    private Path schemaPluginsDir;
    private Path metadataDataDir;
    private Path metadataRevisionDir;
    private Path resourcesDir;
    private Path htmlCacheDir;
    private Path uploadDir;
    private Path formatterDir;
    private String nodeId;

    @Autowired
    private ConfigurableApplicationContext _applicationContext;
    private boolean isDefaultNode;

    /**
     * Check and create if needed GeoNetwork data directory.
     *
     * The data directory is the only mandatory value. If not set, the default location is
     * {@link #getDefaultDataDir(java.nio.file.Path)}.
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
    public void init(final String webappName, final Path webappDir,
                     final ServiceConfig handlerConfig, final JeevesServlet jeevesServlet) throws IOException {
        if (Log.isDebugEnabled(Geonet.DATA_DIRECTORY)) {
            Log.debug(Geonet.DATA_DIRECTORY, "Check and create if needed GeoNetwork data directory");
        }
        this.webappDir = webappDir;
        if (_applicationContext == null) {
            this.nodeId = "srv";
            this.isDefaultNode = true;
        } else {
            final NodeInfo nodeInfo = _applicationContext.getBean(NodeInfo.class);
            this.isDefaultNode = nodeInfo.isDefaultNode();
            this.nodeId = nodeInfo.getId();
        }
        setDataDirectory(jeevesServlet, webappName, handlerConfig);
    }
    public void init(final String webappName, final Path webappDir,  Path systemDataDir,
                     final ServiceConfig handlerConfig, final JeevesServlet jeevesServlet) throws IOException {
        this.systemDataDir = systemDataDir;
        this.init(webappName, webappDir, handlerConfig, jeevesServlet);
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
    private Path lookupProperty(JeevesServlet jeevesServlet, ServiceConfig handlerConfig, String key) {

        final String[] typeStrs = { "Java environment variable ",
                "Servlet context parameter ", "Config.xml appHandler parameter", "System environment variable " };

        String dataDirStr = null;

        if (Log.isDebugEnabled(Geonet.DATA_DIRECTORY)) {
            Log.debug(Geonet.DATA_DIRECTORY, "lookupProperty " + key + " for node " + nodeId);
        }

        final String keyWithNode = nodeId + "." + key;

        boolean useKeyWithNode = true;
        // Loop over variable access methods
        for (int j = 0; j < typeStrs.length && dataDirStr == null; j++) {
            String value = null;
            String typeStr = typeStrs[j];

            String keyToUse = useKeyWithNode ? keyWithNode : key;
            // Lookup section
            switch (j) {
                case 0:
                    value = System.getProperty(keyToUse);
                    break;
                case 1:
                    if (jeevesServlet != null) {
                        value = jeevesServlet.getInitParameter(keyToUse);
                    }
                    break;
                case 2:
                    value = handlerConfig.getValue(keyToUse);
                    break;
                case 3:
//				Environment variable names used by the utilities in the Shell and Utilities 
//				volume of IEEE Std 1003.1-2001 consist solely of uppercase letters, digits, and the '_' 
//				Instead of looking for geonetwork.dir, get geonetwork_dir
                    value = System.getenv(keyWithNode.replace('.', '_'));
                    break;
                default:
                    throw new IllegalArgumentException("Did not expect value: " + j);
            }

            if (value == null || value.equalsIgnoreCase("")) {
                if (useKeyWithNode && j == typeStrs.length - 1) {
                    j = -1;
                    useKeyWithNode = false;
                }
                continue;
            }

            if (Log.isDebugEnabled(Geonet.DATA_DIRECTORY)) {
                Log.debug(Geonet.DATA_DIRECTORY, " Found " + typeStr + "for " + keyToUse
                                                 + " with value " + value);
            }

            dataDirStr = value;
        }

        return dataDirStr == null ? null : IO.toPath(dataDirStr);
    }

    private Path setDataDirectory(JeevesServlet jeevesServlet, String webappName,
                                  ServiceConfig handlerConfig) throws IOException {

        if (this.systemDataDir == null) {
            // System property defined according to webapp name
            this.systemDataDir = lookupProperty(jeevesServlet, handlerConfig, webappName + KEY_SUFFIX);
        }
        // GEONETWORK.dir is default
        if (this.systemDataDir == null) {
            this.systemDataDir = lookupProperty(jeevesServlet, handlerConfig, GEONETWORK_DIR_KEY);
        }

        boolean useDefaultDataDir = false;
        Log.info(Geonet.DATA_DIRECTORY, "   - Data directory initialization: " + this.systemDataDir);

        if (this.systemDataDir == null) {
            Log.warning(Geonet.DATA_DIRECTORY,
                    "    - Data directory properties is not set. Use "
                    + webappName + KEY_SUFFIX + " or " + GEONETWORK_DIR_KEY
                    + " properties.");
            useDefaultDataDir = true;
        } else {
            updateSystemDataDirWithNodeSuffix();
            try {
                Files.createDirectories(this.systemDataDir);
            } catch (IOException e) {
                Log.error(Geonet.DATA_DIRECTORY, "Error creating system data directory: " + this.systemDataDir);
                useDefaultDataDir = true;
            }

            if (!Files.exists(this.systemDataDir)) {
                Log.warning(Geonet.DATA_DIRECTORY,
                        "    - Data directory does not exist. Create it first.");
                useDefaultDataDir = true;
            }

            try {
                final Path testFile = this.systemDataDir.resolve("testDD.txt");
                IO.touch(testFile);
                Files.delete(testFile);
            } catch (IOException e) {
                Log.warning(
                        Geonet.DATA_DIRECTORY,
                        "    - Data directory is not writable. Set read/write privileges to user starting the catalogue (ie. "
                        + System.getProperty("user.name") + ").");
                useDefaultDataDir = true;
            }

            if (!this.systemDataDir.isAbsolute()) {
                Log.warning(
                        Geonet.DATA_DIRECTORY,
                        "    - Data directory is not an absolute path. Relative path is not recommended.\n"
                        + "Update "
                        + webappName
                        + KEY_SUFFIX + " or geonetwork.dir environment variable.");
            }
        }

        if (useDefaultDataDir) {
            systemDataDir = getDefaultDataDir(webappDir);
            updateSystemDataDirWithNodeSuffix();
            Log.warning(Geonet.DATA_DIRECTORY,
                    "    - Data directory provided could not be used. Using default location: "
                    + systemDataDir);
        }


        try {
            this.systemDataDir = this.systemDataDir.toRealPath();
            if (!Files.exists(this.systemDataDir)) {
                Log.error(Geonet.DATA_DIRECTORY, "System Data Directory does not exist");
            }
        } catch (IOException e) {
            Log.warning(Geonet.DATA_DIRECTORY, "Unable to make a canonical path from: " + systemDataDir);
        }
        Log.info(Geonet.DATA_DIRECTORY, "   - Data directory is: "
                                        + systemDataDir);

        // Set subfolder data directory
        luceneDir = setDir(jeevesServlet, webappName, handlerConfig, ".lucene" + KEY_SUFFIX,
                Geonet.Config.LUCENE_DIR, "index");
        spatialIndexPath = setDir(jeevesServlet, "", handlerConfig, "spatial" + KEY_SUFFIX,
                null, "spatialindex");

        configDir = setDir(jeevesServlet, webappName, handlerConfig, ".config" + KEY_SUFFIX,
                Geonet.Config.CONFIG_DIR, "config");
        thesauriDir = setDir(jeevesServlet, webappName, handlerConfig,
                ".codeList" + KEY_SUFFIX, Geonet.Config.CODELIST_DIR, "config", "codelist"
        );
        schemaPluginsDir = setDir(jeevesServlet, webappName, handlerConfig, ".schema" + KEY_SUFFIX,
                Geonet.Config.SCHEMAPLUGINS_DIR, "config", "schema_plugins"
        );
        metadataDataDir = setDir(jeevesServlet, webappName, handlerConfig, ".data" + KEY_SUFFIX,
                Geonet.Config.DATA_DIR, "data", "metadata_data"
        );
        metadataRevisionDir = setDir(jeevesServlet, webappName, handlerConfig, ".svn" + KEY_SUFFIX,
                Geonet.Config.SUBVERSION_PATH, "data", "metadata_subversion"
        );
        resourcesDir = setDir(jeevesServlet, webappName, handlerConfig,
                ".resources" + KEY_SUFFIX, Geonet.Config.RESOURCES_DIR, "data", "resources"
        );
        uploadDir = setDir(jeevesServlet, webappName, handlerConfig,
                ".upload" + KEY_SUFFIX, Geonet.Config.UPLOAD_DIR, "data", "upload"
        );
		formatterDir = setDir(jeevesServlet, webappName, handlerConfig,
                ".formatter" + KEY_SUFFIX, Geonet.Config.FORMATTER_PATH, "data", "formatter");

        htmlCacheDir = setDir(jeevesServlet, webappName, handlerConfig,
                ".htmlcache" + KEY_SUFFIX, Geonet.Config.HTMLCACHE_DIR, handlerConfig.getValue(Geonet.Config.RESOURCES_DIR), "htmlcache"
        );

        handlerConfig.setValue(Geonet.Config.SYSTEM_DATA_DIR, this.systemDataDir.toString());

        initDataDirectory();

        return this.systemDataDir;
    }

    private void updateSystemDataDirWithNodeSuffix() {
        if (!isDefaultNode) {

            final String newName = this.systemDataDir.getFileName().toString() + '_' + this.nodeId;
            this.systemDataDir = this.systemDataDir.getParent().resolve(newName);
        }
    }

    /**
	 * Checks if data directory is empty or not. If empty, add mandatory
	 * elements (ie. codelist).
	 */
    private void initDataDirectory() throws IOException {
        Log.info(Geonet.DATA_DIRECTORY, "   - Data directory initialization ...");

        if (!Files.exists(this.thesauriDir) || IO.isEmptyDir(this.thesauriDir)) {
            Log.info(Geonet.DATA_DIRECTORY, "     - Copying codelists directory ..." + thesauriDir);
            try {
                final Path srcThesauri = getDefaultDataDir(webappDir).resolve("config").resolve("codelist");
                IO.copyDirectoryOrFile(srcThesauri, this.thesauriDir, false);
            } catch (IOException e) {
                Log.error(Geonet.DATA_DIRECTORY, "     - Thesaurus copy failed: " + e.getMessage(), e);
            }
        }

        // Copy default logo to the harvesting folder
        Path logoDir = this.resourcesDir.resolve("images").resolve("harvesting");
        if (!Files.exists(logoDir) || IO.isEmptyDir(logoDir)) {
            Log.info(Geonet.DATA_DIRECTORY, "     - Copying logos ...");
            try {
                final Path srcLogo = this.webappDir.resolve("images").resolve("harvesting");

                if (Files.exists(srcLogo)) {
                    try (DirectoryStream<Path> paths = Files.newDirectoryStream(srcLogo)) {
                        for (Path path : paths) {
                            final Path relativePath = srcLogo.relativize(path);
                            final Path dest = logoDir.resolve(relativePath.toString());
                            if (!Files.exists(dest)) {
                                IO.copyDirectoryOrFile(path, dest, false);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                Log.error(Geonet.DATA_DIRECTORY, "     - Logo copy failed: " + e.getMessage(), e);
            }
        }
        Path schemaCatFile = configDir.resolve(Geonet.File.SCHEMA_PLUGINS_CATALOG);
        if (!Files.exists(schemaCatFile)) {
            Log.info(Geonet.DATA_DIRECTORY, "     - Copying schema plugin catalogue ...");
            try {
                final Path srcFile = webappDir.resolve("WEB-INF").resolve(Geonet.File.SCHEMA_PLUGINS_CATALOG);
                IO.copyDirectoryOrFile(srcFile, schemaCatFile, false);

                // Copy missing schema plugins
                Path srcPluginsDir = getDefaultDataDir(webappDir).resolve("config").resolve("schema_plugins");
                try (DirectoryStream<Path> schemaPlugins = Files.newDirectoryStream(srcPluginsDir)) {
                    final Iterator<Path> pathIterator = schemaPlugins.iterator();
                    while (pathIterator.hasNext()) {
                        Path next = pathIterator.next();
                        Path destDir = this.schemaPluginsDir.resolve(next.getFileName().toString());
                        if (!Files.exists(destDir)) {
                            IO.copyDirectoryOrFile(next, destDir, false);
                        }
                    }
                }

            } catch (IOException e) {
                Log.info(
                        Geonet.DATA_DIRECTORY,
                        "      - Error copying schema plugin catalogue: "
                        + e.getMessage());
            }
        }

        final Path locDir = webappDir.resolve("loc");
        if (!Files.exists(locDir)) {
            Files.createDirectories(locDir);
        }
	}

    private Path getDefaultDataDir(Path webappDir) {
        return webappDir.resolve("WEB-INF").resolve("data");
    }

    /**
     * Try to retrieve from system properties the variable with name
     * <webapp.name>.key. If not set, create the resource folder using
     * <geonetwork.dir>/folder and set the system property value. Create the
     * folder if does not exist.
     *   @param jeevesServlet
     * @param webappName
     * @param handlerConfig
     * @param key
     * @param handlerKey       @return
     * @param firstPathSeg    */
    private Path setDir(JeevesServlet jeevesServlet, String webappName,
                        ServiceConfig handlerConfig, String key, String handlerKey, String firstPathSeg, String... otherSegments) {
        String envKey = webappName + key;
        Path dir = lookupProperty(jeevesServlet, handlerConfig, envKey);
        if (dir == null) {
            dir = this.systemDataDir.resolve(firstPathSeg);
            for (String otherSegment : otherSegments) {
                dir = dir.resolve(otherSegment);
            }
        } else {
            if (!dir.isAbsolute()) {
                Log.info(Geonet.DATA_DIRECTORY, "    - " + envKey
                                                + " for directory " + dir
                                                + " is relative path. Use absolute path instead.");
            }
        }
        if(handlerKey != null) {
            handlerConfig.setValue(handlerKey, dir.toString());
        }
        // Create directory if it does not exist
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Log.info(Geonet.DATA_DIRECTORY, "    - " + envKey + " is " + dir);
        return dir;
    }

    /**
     * Get the root data dir for Geonetwork.  Typically other "data" directories are subdirectories to this.
     *
     * @return the root data dir for Geonetwork
     */
    public Path getSystemDataDir() {
        return systemDataDir;
    }
    /**
     * Set the root data dir for Geonetwork.  Typically other "data" directories are subdirectories to this.
     */
    public void setSystemDataDir(Path systemDataDir) {
        this.systemDataDir = systemDataDir;
    }
    /**
     * Get the directory to store the lucene indices in.
     * @return The directory to store the lucene indices in.
     */
    public Path getLuceneDir() {
        return luceneDir;
    }
    /**
     * Set the directory to store the lucene indices in.
     */
    public void setLuceneDir(Path luceneDir) {
        this.luceneDir = luceneDir;
    }

    /**
     * Get the directory to store the metadata spatial index. If the spatial index is to be stored locally this is the directory to use.
     *
     * @return the directory to store the metadata spatial index
     */
    public Path getSpatialIndexPath() {
        return spatialIndexPath;
    }
    /**
     * Set the directory to store the metadata spatial index. If the spatial index is to be stored locally this is the directory to use.
     */
    public void setSpatialIndexPath(Path spatialIndexPath) {
        this.spatialIndexPath = spatialIndexPath;
    }

    /**
     * Return the directory containing the configuration file.
     *
     * @return the directory containing the configuration file.
     */
    public Path getConfigDir() {
        return configDir;
    }
    /**
     * Set the directory containing the configuration file.
     */
    public void setConfigDir(Path configDir) {
        this.configDir = configDir;
    }
    /**
     * Get the directory containing the thesauri.
     *
     * @return the directory containing the thesauri.
     */
    public Path getThesauriDir() {
        return thesauriDir;
    }
    /**
     * Set the directory containing the thesauri.
     */
    public void setThesauriDir(Path thesauriDir) {
        this.thesauriDir = thesauriDir;
    }

    /**
     * Get the schema plugins directory.
     *
     * @return the schema plugins directory.
     */
    public Path getSchemaPluginsDir() {
        return schemaPluginsDir;
    }
    /**
     * Set the schema plugins directory.
     */
    public void setSchemaPluginsDir(Path schemaPluginsDir) {
        this.schemaPluginsDir = schemaPluginsDir;
    }
    /**
     * Get the directory that contain all the resources related to metadata (thumbnails, attachments, etc...).
     *
     * @return the directory that contain all the resources related to metadata (thumbnails, attachments, etc...).
     */
    public Path getMetadataDataDir() {
        return metadataDataDir;
    }
    /**
     * Set the directory that contain all the resources related to metadata (thumbnails, attachments, etc...).
     */
    public void setMetadataDataDir(Path metadataDataDir) {
        this.metadataDataDir = metadataDataDir;
    }
    /**
     * Get the directory containing the metadata revision history if it is to be stored locally.
     * @return the directory containing the metadata revision history.
     */
    public Path getMetadataRevisionDir() {
        return metadataRevisionDir;
    }
    /**
     * Set the directory containing the metadata revision history if it is to be stored locally.
     */
    public void setMetadataRevisionDir(Path metadataRevisionDir) {
        this.metadataRevisionDir = metadataRevisionDir;
    }
    /**
     * Get the directory that will contain the resources for the system.
     *
     * @see org.fao.geonet.resources.ResourceFilter
     * @return the directory that will contain the resources for the system.
     */
    public Path getResourcesDir() {
        return resourcesDir;
    }
    /**
     * Set the directory that will contain the resources for the system.
     */
    public void setResourcesDir(Path resourcesDir) {
        this.resourcesDir = resourcesDir;
    }

    /**
     * Get the directory containing the webapplication.
     *
     * @return the directory containing the webapplication.
     */
    public Path getWebappDir() {
        return webappDir;
    }

    /**
     * Get directory for caching html data.
     *
     * @return directory for caching html data.
     */
    public Path getHtmlCacheDir() {
        return htmlCacheDir;
    }

    /**
     * Get directory for caching where uploaded files go.
     *
     * @return directory for caching html data.
     */
    public Path getUploadDir() {
        return uploadDir;
    }
    /**
     * Set directory for caching html data.
     */
    public void setHtmlCacheDir(Path htmlCacheDir) {
        this.htmlCacheDir = htmlCacheDir;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Path getFormatterDir() {
        return formatterDir;
    }

    public void setFormatterDir(Path formatterDir) {
        this.formatterDir = formatterDir;
    }

    public Path resolveWebResource(String resourcePath) {
        if (resourcePath.charAt(0) == '/' || resourcePath.charAt(0) == '\\') {
            resourcePath = resourcePath.substring(1);
        }
        return this.webappDir.resolve(resourcePath);
    }
}

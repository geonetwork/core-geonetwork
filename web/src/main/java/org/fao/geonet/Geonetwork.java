//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet;

import com.vividsolutions.jts.geom.MultiPolygon;
import jeeves.config.springutil.ServerBeanPropertyUpdater;
import jeeves.interfaces.ApplicationHandler;
import jeeves.server.JeevesEngine;
import jeeves.server.JeevesProxyInfo;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.sources.http.ServletPathFinder;
import jeeves.xlink.Processor;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.XmlSerializerSvn;
import org.fao.geonet.kernel.csw.CswHarvesterResponseExecutionService;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.thumbnail.ThumbnailMaker;
import org.fao.geonet.languages.LanguageDetector;
import org.fao.geonet.lib.DbLib;
import org.fao.geonet.notifier.MetadataNotifierControl;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.services.util.z3950.Repositories;
import org.fao.geonet.services.util.z3950.Server;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.util.ThreadUtils;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.ProxyInfo;
import org.fao.geonet.utils.XmlResolver;
import org.geotools.data.DataStore;
import org.geotools.data.shapefile.indexed.IndexType;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * This is the main class, it handles http connections and inits the system.
 */
public class Geonetwork implements ApplicationHandler {
    private Logger logger;
    private Path appPath;
    private SearchManager searchMan;
    private MetadataNotifierControl metadataNotifierControl;
    private ThreadPool threadPool;
    private ConfigurableApplicationContext _applicationContext;

    //---------------------------------------------------------------------------
    //---
    //--- GetContextName
    //---
    //---------------------------------------------------------------------------

    public String getContextName() {
        return Geonet.CONTEXT_NAME;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Start
    //---
    //---------------------------------------------------------------------------

    /**
     * Inits the engine, loading all needed data.
     */
    public Object start(Element config, ServiceContext context) throws Exception {
        logger = context.getLogger();
        this._applicationContext = context.getApplicationContext();
        ConfigurableListableBeanFactory beanFactory = context.getApplicationContext().getBeanFactory();

        ServletPathFinder finder = new ServletPathFinder(this._applicationContext.getBean(ServletContext.class));
        appPath = finder.getAppPath();
        String baseURL = context.getBaseUrl();
        String webappName = baseURL.substring(1);
        // TODO : if webappName is "". ie no context

        final SystemInfo systemInfo = _applicationContext.getBean(SystemInfo.class);
        String version = systemInfo.getVersion();
        String subVersion = systemInfo.getSubVersion();

        logger.info("Initializing GeoNetwork " + version + "." + subVersion + " ...");

        // Get main service config handler
        @SuppressWarnings("unchecked")
        List<Element> serviceConfigElems = config.getChildren();
        ServiceConfig handlerConfig = new ServiceConfig(serviceConfigElems);

        // Init configuration directory
        final GeonetworkDataDirectory dataDirectory = _applicationContext.getBean(GeonetworkDataDirectory.class);
        dataDirectory.init(webappName, appPath, handlerConfig, context.getServlet());

        // Get config handler properties
        String systemDataDir = handlerConfig.getMandatoryValue(Geonet.Config.SYSTEM_DATA_DIR);
        String thesauriDir = handlerConfig.getMandatoryValue(Geonet.Config.CODELIST_DIR);
        String luceneDir = handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_DIR);
        String luceneConfigXmlFile = handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_CONFIG);

        logger.info("Data directory: " + systemDataDir);

        setProps(appPath, handlerConfig);

        importDatabaseData(context);

        // Status actions class - load it
        String statusActionsClassName = handlerConfig.getMandatoryValue(Geonet.Config.STATUS_ACTIONS_CLASS);
        @SuppressWarnings("unchecked")
        Class<StatusActions> statusActionsClass = (Class<StatusActions>) Class.forName(statusActionsClassName);

        JeevesJCS.setConfigFilename(appPath.resolve("WEB-INF/classes/cache.ccf"));

        // force caches to be config'd so shutdown hook works correctly
        JeevesJCS.getInstance(Processor.XLINK_JCS);
        JeevesJCS.getInstance(XmlResolver.XMLRESOLVER_JCS);

        //------------------------------------------------------------------------
        //--- initialize thread pool

        logger.info("  - Thread Pool...");

        threadPool = new ThreadPool();

        //------------------------------------------------------------------------
        //--- initialize settings subsystem

        logger.info("  - Setting manager...");

        SettingManager settingMan = this._applicationContext.getBean(SettingManager.class);

        //--- initialize ThreadUtils with setting manager and rm props
        final DataSource dataSource = context.getBean(DataSource.class);
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            ThreadUtils.init(conn.getMetaData().getURL(), settingMan);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }


        //------------------------------------------------------------------------
        //--- initialize Z39.50

        logger.info("  - Z39.50...");

        boolean z3950Enable = settingMan.getValueAsBool("system/z3950/enable", false);
        String z3950port = settingMan.getValue("system/z3950/port");

        logger.info("     - Z39.50 is enabled: " + z3950Enable);
        if (z3950Enable) {
            // build Z3950 repositories file first from template
            URL url = getClass().getClassLoader().getResource(Geonet.File.JZKITCONFIG_TEMPLATE);

            if (Repositories.build(url, context)) {
                logger.info("     Repositories file built from template.");

                try {
                    ConfigurableApplicationContext appContext = context.getApplicationContext();

                    // to have access to the GN context in spring-managed objects
                    ContextContainer cc = (ContextContainer) appContext.getBean("ContextGateway");
                    cc.setSrvctx(context);

                    if (!z3950Enable) {
                        logger.info("     Server is Disabled.");
                    } else {
                        logger.info("     Server is Enabled.");

                        Server.init(z3950port, appContext);
                    }
                } catch (Exception e) {
                    logger.error("     Repositories file init FAILED - Z3950 server disabled and Z3950 client services (remote search, " +
                                 "harvesting) may not work. Error is:" + e.getMessage());
                    e.printStackTrace();
                }

            } else {
                logger.error("     Repositories file builder FAILED - Z3950 server disabled and Z3950 client services (remote search, " +
                             "harvesting) may not work.");
            }
        }
        //------------------------------------------------------------------------
        //--- initialize SchemaManager

        logger.info("  - Schema manager...");

        Path schemaPluginsDir = dataDirectory.getSchemaPluginsDir();
        Path schemaCatalogueFile = dataDirectory.getConfigDir().resolve(Geonet.File.SCHEMA_PLUGINS_CATALOG);
        boolean createOrUpdateSchemaCatalog = handlerConfig.getMandatoryValue(Geonet.Config.SCHEMA_PLUGINS_CATALOG_UPDATE).equals("true");
        logger.info("			- Schema plugins directory: " + schemaPluginsDir);
        logger.info("			- Schema Catalog File     : " + schemaCatalogueFile);
        SchemaManager schemaMan = _applicationContext.getBean(SchemaManager.class);
        schemaMan.configure(_applicationContext, appPath, dataDirectory.getResourcesDir(), schemaCatalogueFile,
                schemaPluginsDir, context.getLanguage(), handlerConfig.getMandatoryValue(Geonet.Config.PREFERRED_SCHEMA),
                createOrUpdateSchemaCatalog);

        //------------------------------------------------------------------------
        //--- initialize search and editing

        logger.info("  - Search...");

        boolean logSpatialObject = "true".equalsIgnoreCase(handlerConfig.getMandatoryValue(Geonet.Config.STAT_LOG_SPATIAL_OBJECTS));
        boolean logAsynch = "true".equalsIgnoreCase(handlerConfig.getMandatoryValue(Geonet.Config.STAT_LOG_ASYNCH));
        logger.info("  - Log spatial object: " + logSpatialObject);
        logger.info("  - Log in asynch mode: " + logAsynch);

        String luceneTermsToExclude = "";
        luceneTermsToExclude = handlerConfig.getMandatoryValue(Geonet.Config.STAT_LUCENE_TERMS_EXCLUDE);

        LuceneConfig lc = _applicationContext.getBean(LuceneConfig.class);
        lc.configure(luceneConfigXmlFile);
        logger.info("  - Lucene configuration is:");
        logger.info(lc.toString());

        try {
            _applicationContext.getBean(DataStore.class);
        } catch (NoSuchBeanDefinitionException e) {
            DataStore dataStore = createShapefileDatastore(luceneDir);
            _applicationContext.getBeanFactory().registerSingleton("dataStore", dataStore);
            //--- no datastore for spatial indexing means that we can't continue
            if (dataStore == null) {
                throw new IllegalArgumentException("GeoTools datastore creation failed - check logs for more info/exceptions");
            }
        }

        String maxWritesInTransactionStr = handlerConfig.getMandatoryValue(Geonet.Config.MAX_WRITES_IN_TRANSACTION);
        int maxWritesInTransaction = SpatialIndexWriter.MAX_WRITES_IN_TRANSACTION;
        try {
            maxWritesInTransaction = Integer.parseInt(maxWritesInTransactionStr);
        } catch (NumberFormatException nfe) {
            logger.error("Invalid config parameter: maximum number of writes to spatial index in a transaction (maxWritesInTransaction)"
                         + ", Using " + maxWritesInTransaction + " instead.");
            nfe.printStackTrace();
        }

        SettingInfo settingInfo = context.getBean(SettingInfo.class);
        searchMan = _applicationContext.getBean(SearchManager.class);
        searchMan.init(logAsynch,
                logSpatialObject, luceneTermsToExclude,
                maxWritesInTransaction);


        // if the validator exists the proxyCallbackURL needs to have the external host and
        // servlet name added so that the cas knows where to send the validation notice
        ServerBeanPropertyUpdater.updateURL(settingInfo.getSiteUrl(true) + baseURL, _applicationContext);

        //------------------------------------------------------------------------
        //--- extract intranet ip/mask and initialize AccessManager

        logger.info("  - Access manager...");

        //------------------------------------------------------------------------
        //--- get edit params and initialize DataManager

        logger.info("  - Xml serializer and Data manager...");

        SvnManager svnManager = _applicationContext.getBean(SvnManager.class);
        XmlSerializer xmlSerializer = _applicationContext.getBean(XmlSerializer.class);

        if (xmlSerializer instanceof XmlSerializerSvn && svnManager != null) {
            svnManager.setContext(context);
            Path subversionPath = dataDirectory.getMetadataRevisionDir().toAbsolutePath().normalize();
            svnManager.setSubversionPath(subversionPath.toString());
            svnManager.init();
        }

        /**
         * Initialize language detector
         */
        LanguageDetector.init(appPath.resolve(_applicationContext.getBean(Geonet.Config.LANGUAGE_PROFILES_DIR, String.class)));

        //------------------------------------------------------------------------
        //--- Initialize thesaurus

        logger.info("  - Thesaurus...");

        _applicationContext.getBean(ThesaurusManager.class).init(context, thesauriDir);


        //------------------------------------------------------------------------
        //--- initialize catalogue services for the web

        logger.info("  - Open Archive Initiative (OAI-PMH) server...");

        OaiPmhDispatcher oaipmhDis = new OaiPmhDispatcher(settingMan, schemaMan);


        GeonetContext gnContext = new GeonetContext(_applicationContext, false, statusActionsClass, threadPool);

        //------------------------------------------------------------------------
        //--- return application context

        beanFactory.registerSingleton("serviceHandlerConfig", handlerConfig);
        beanFactory.registerSingleton("oaipmhDisatcher", oaipmhDis);


        _applicationContext.getBean(DataManager.class).init(context, false);
        _applicationContext.getBean(HarvestManager.class).init(context, gnContext.isReadOnly());

        _applicationContext.getBean(ThumbnailMaker.class).init(context);

        logger.info("Site ID is : " + settingMan.getSiteId());

        // Creates a default site logo, only if the logo image doesn't exists
        // This can happen if the application has been updated with a new version preserving the database and
        // images/logos folder is not copied from old application
        createSiteLogo(settingMan.getSiteId(), context, context.getAppPath());


        // Notify unregistered metadata at startup. Needed, for example, when the user enables the notifier config
        // to notify the existing metadata in database
        // TODO: Fix DataManager.getUnregisteredMetadata and uncomment next lines
        metadataNotifierControl = new MetadataNotifierControl(context);
        metadataNotifierControl.runOnce();

        //--- load proxy information from settings into Jeeves for observers such
        //--- as jeeves.utils.XmlResolver to use
        ProxyInfo pi = JeevesProxyInfo.getInstance();
        boolean useProxy = settingMan.getValueAsBool("system/proxy/use", false);
        if (useProxy) {
            String proxyHost = settingMan.getValue("system/proxy/host");
            String proxyPort = settingMan.getValue("system/proxy/port");
            String username = settingMan.getValue("system/proxy/username");
            String password = settingMan.getValue("system/proxy/password");
            pi.setProxyInfo(proxyHost, Integer.valueOf(proxyPort), username, password);
        }

        //
        // db heartbeat configuration -- for failover to readonly database
        //
        boolean dbHeartBeatEnabled = Boolean.parseBoolean(handlerConfig.getValue(Geonet.Config.DB_HEARTBEAT_ENABLED, "false"));
        if (dbHeartBeatEnabled) {
            Integer dbHeartBeatInitialDelay = Integer.parseInt(handlerConfig.getValue(Geonet.Config.DB_HEARTBEAT_INITIALDELAYSECONDS,
                    "5"));
            Integer dbHeartBeatFixedDelay = Integer.parseInt(handlerConfig.getValue(Geonet.Config.DB_HEARTBEAT_FIXEDDELAYSECONDS, "60"));
            createDBHeartBeat(gnContext, dbHeartBeatInitialDelay, dbHeartBeatFixedDelay);
        }
        return gnContext;
    }

    private void importDatabaseData(final ServiceContext context) {
        // check if database has any data
        final SettingRepository settingRepository = context.getBean(SettingRepository.class);
        final long count = settingRepository.count();
        if (count == 0) {
            try {
                // import data from init files
                List<Pair<String, String>> importData = (List) context.getApplicationContext().getBean("initial-data");
                    final DbLib dbLib = new DbLib();
                    for (Pair<String, String> pair : importData) {
                        final ServletContext servletContext = context.getServlet().getServletContext();
                        final Path appPath = context.getAppPath();
                        final Path filePath = IO.toPath(pair.one());
                        final String filePrefix = pair.two();
                        Log.warning(Geonet.DB, "Executing SQL from: " + filePath + " " + filePrefix);
                        dbLib.insertData(servletContext, context, appPath, filePath, filePrefix);
                    }
                String siteUuid = UUID.randomUUID().toString();
                context.getBean(SettingManager.class).setSiteUuid(siteUuid);

                // Reload services which may be defined in
                // database creation scripts in Services table.
                context.getBean(JeevesEngine.class).loadConfigDB(context.getApplicationContext(), -1);
            } catch (Throwable t) {
                Log.error(Geonet.DB, "Error occurred while trying to execute SQL", t);
                throw new RuntimeException(t);
            }
        }
    }

    /**
     * Sets up a periodic check whether GeoNetwork can successfully write to the database. If it can't, GeoNetwork will
     * automatically switch to read-only mode.
     */
    private void createDBHeartBeat(final GeonetContext gc, Integer initialDelay, Integer fixedDelay) throws SchedulerException {
        logger.info("creating DB heartbeat with initial delay of " + initialDelay + " s and fixed delay of " + fixedDelay + " s");
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable DBHeartBeat = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean readOnly = gc.isReadOnly();
                    logger.debug("DBHeartBeat: GN is read-only ? " + readOnly);
                    boolean canWrite = checkDBWrite();
                    HarvestManager hm = gc.getBean(HarvestManager.class);
                    if (readOnly && canWrite) {
                        logger.warning("GeoNetwork can write to the database, switching to read-write mode");
                        readOnly = false;
                        gc.setReadOnly(readOnly);
                        hm.setReadOnly(readOnly);
                    } else if (!readOnly && !canWrite) {
                        logger.warning("GeoNetwork can not write to the database, switching to read-only mode");
                        readOnly = true;
                        gc.setReadOnly(readOnly);
                        hm.setReadOnly(readOnly);
                    } else {
                        if (readOnly) {
                            logger.info("GeoNetwork remains in read-only mode");
                        } else {
                            logger.debug("GeoNetwork remains in read-write mode");
                        }
                    }
                } catch (Throwable x) {
                    // any uncaught exception would cause the scheduled execution to silently stop
                    logger.error("DBHeartBeat error: " + x.getMessage() + " This error is ignored.");
                    x.printStackTrace();
                }
            }

            private boolean checkDBWrite() {
                SettingRepository settingsRepo = gc.getBean(SettingRepository.class);
                try {
                    Setting newSetting = settingsRepo.save(new Setting().setName("DBHeartBeat").setValue("value"));
                    settingsRepo.flush();
                    settingsRepo.delete(newSetting);
                    return true;
                } catch (Exception x) {
                    logger.info("DBHeartBeat Exception: " + x.getMessage());
                    return false;
                }
            }
        };
        scheduledExecutorService.scheduleWithFixedDelay(DBHeartBeat, initialDelay, fixedDelay, TimeUnit.SECONDS);
    }

    /**
     * Creates a default site logo, only if the logo image doesn't exists
     *
     * @param nodeUuid
     * @param context
     * @param appPath
     */
    private void createSiteLogo(String nodeUuid, ServiceContext context, Path appPath) {
        try {
            Path logosDir = Resources.locateLogosDir(context);
            Path logo =logosDir.resolve(nodeUuid + ".gif");
            if (!Files.exists(logo)) {
                final ServletContext servletContext = context.getServlet().getServletContext();
                byte[] logoData = Resources.loadImage(servletContext, appPath, "images/logos/dummy.gif", new byte[0]).one();
                Files.write(logo, logoData);
            }
        } catch (Throwable e) {
            logger.error("      Error when setting the logo: " + e.getMessage());
        }
    }

    /**
     * Set system properties to those required
     *
     * @param webappDir webapp path
     */
    private void setProps(Path webappDir, ServiceConfig handlerConfig) {

        final Path configDir = IO.toPath(handlerConfig.getValue(Geonet.Config.CONFIG_DIR));
        final Path schemapluginUriCatalog = configDir.resolve("schemaplugin-uri-catalog.xml");
        Path webInf = SchemaManager.registerXmlCatalogFiles(webappDir, schemapluginUriCatalog);

        //--- Set mime-mappings
        String mimeProp = System.getProperty("mime-mappings");
        if (mimeProp == null) mimeProp = "";
        if (!mimeProp.equals("")) {
            logger.info("Overriding mime-mappings property (was set to " + mimeProp + ")");
        }
        mimeProp = webInf.resolve("mime-types.properties").toString();
        System.setProperty("mime-mappings", mimeProp);
        logger.info("mime-mappings property set to " + mimeProp);

    }


    //---------------------------------------------------------------------------
    //---
    //--- Stop
    //---
    //---------------------------------------------------------------------------

    public void stop() {
        logger.info("Stopping geonetwork...");

        logger.info("shutting down CSW HarvestResponse executionService");
        CswHarvesterResponseExecutionService.getExecutionService().shutdownNow();

        //------------------------------------------------------------------------
        //--- end search
        logger.info("  - search...");

        try {
            searchMan.end();
        } catch (Exception e) {
            logger.error("Raised exception while stopping search");
            logger.error("  Exception : " + e);
            logger.error("  Message   : " + e.getMessage());
            logger.error("  Stack     : " + Util.getStackTrace(e));
        }


        logger.info("  - ThreadPool ...");
        threadPool.shutDown();

        logger.info("  - MetadataNotifier ...");
        try {
            metadataNotifierControl.shutDown();
        } catch (Exception e) {
            logger.error("Raised exception while stopping metadatanotifier");
            logger.error("  Exception : " + e);
            logger.error("  Message   : " + e.getMessage());
            logger.error("  Stack     : " + Util.getStackTrace(e));
        }


        logger.info("  - Harvest Manager...");
        _applicationContext.getBean(HarvestManager.class).shutdown();

        logger.info("  - Z39.50...");
        Server.end();
    }

    //---------------------------------------------------------------------------

    private DataStore createShapefileDatastore(String indexDir) throws Exception {

        File file = new File(indexDir + "/" + SpatialIndexWriter._SPATIAL_INDEX_TYPENAME + ".shp");
        if (!file.getParentFile().mkdirs() && !file.getParentFile().exists()) {
            throw new RuntimeException("Unable to create the spatial index (shapefile) directory: " + file.getParentFile());
        }
        if (!file.exists()) {
            logger.info("Creating shapefile " + file.getAbsolutePath());
        } else {
            logger.info("Using shapefile " + file.getAbsolutePath());
        }
        IndexedShapefileDataStore ids = new IndexedShapefileDataStore(file.toURI().toURL(), new URI("http://geonetwork.org"), false, false, IndexType.QIX, Charset.forName(Constants.ENCODING));
        CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");

        if (crs != null) {
            ids.forceSchemaCRS(crs);
        }

        if (!file.exists()) {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(DefaultGeographicCRS.WGS84).binding(MultiPolygon.class).buildDescriptor("the_geom");
            builder.setName(SpatialIndexWriter._SPATIAL_INDEX_TYPENAME);
            builder.add(geomDescriptor);
            builder.add(SpatialIndexWriter._IDS_ATTRIBUTE_NAME, String.class);
            ids.createSchema(builder.buildFeatureType());
        }

        logger.info("NOTE: Using shapefile for spatial index, this can be slow for larger catalogs");
        return ids;
    }
}

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import jeeves.JeevesJCS;
import jeeves.JeevesProxyInfo;
import jeeves.config.springutil.JeevesApplicationContext;
import jeeves.config.springutil.ServerBeanPropertyUpdater;
import jeeves.constants.Jeeves;
import jeeves.interfaces.ApplicationHandler;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.overrides.ConfigurationOverrides;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.ProxyInfo;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.utils.XmlResolver;
import jeeves.xlink.Processor;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.DataManagerParameter;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.XmlSerializer;
import org.fao.geonet.kernel.XmlSerializerDb;
import org.fao.geonet.kernel.XmlSerializerSvn;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CswHarvesterResponseExecutionService;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.languages.LanguageDetector;
import org.fao.geonet.lib.DatabaseType;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.lib.ServerLib;
import org.fao.geonet.notifier.MetadataNotifierControl;
import org.fao.geonet.notifier.MetadataNotifierManager;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.services.util.z3950.Repositories;
import org.fao.geonet.services.util.z3950.Server;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.util.ThreadUtils;
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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * This is the main class, it handles http connections and inits the system.
  */
public class Geonetwork implements ApplicationHandler {
	private Logger        		logger;
	private String 				path;				
	private SearchManager 		searchMan;
	private MetadataNotifierControl metadataNotifierControl;
	private ThreadPool        threadPool;
	private String   FS         = File.separator;
	private Element dbConfiguration;
    private JeevesApplicationContext _applicationContext;
    private static final String       SPATIAL_INDEX_FILENAME    = "spatialindex";
	private static final String       IDS_ATTRIBUTE_NAME        = "id";

	//---------------------------------------------------------------------------
	//---
	//--- GetContextName
	//---
	//---------------------------------------------------------------------------

	public String getContextName() { return Geonet.CONTEXT_NAME; }

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

		path    = context.getAppPath();
		String baseURL = context.getBaseUrl();
		String webappName = baseURL.substring(1);
		// TODO : if webappName is "". ie no context
		
        ServletContext servletContext = null;
        if (context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
		ServerLib sl = new ServerLib(servletContext, path);
		String version = sl.getVersion();
		String subVersion = sl.getSubVersion();

		logger.info("Initializing GeoNetwork " + version +  "." + subVersion +  " ...");

		// Get main service config handler
		@SuppressWarnings("unchecked")
        List<Element> serviceConfigElems = config.getChildren();
        ServiceConfig handlerConfig = new ServiceConfig(serviceConfigElems);
		
		// Init configuration directory
		new GeonetworkDataDirectory(webappName, path, handlerConfig, context.getServlet());
		
		// Get config handler properties
		String systemDataDir = handlerConfig.getMandatoryValue(Geonet.Config.SYSTEM_DATA_DIR);
		String thesauriDir = handlerConfig.getMandatoryValue(Geonet.Config.CODELIST_DIR);
		String luceneDir =  handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_DIR);
		String dataDir =  handlerConfig.getMandatoryValue(Geonet.Config.DATA_DIR);
		String luceneConfigXmlFile = handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_CONFIG);
		String summaryConfigXmlFile = handlerConfig.getMandatoryValue(Geonet.Config.SUMMARY_CONFIG);

		logger.info("Data directory: " + systemDataDir);

		setProps(path, handlerConfig);

		// Status actions class - load it
		String statusActionsClassName = handlerConfig.getMandatoryValue(Geonet.Config.STATUS_ACTIONS_CLASS); 
		@SuppressWarnings("unchecked")
        Class<StatusActions> statusActionsClass = (Class<StatusActions>) Class.forName(statusActionsClassName);

        String languageProfilesDir = handlerConfig
                .getMandatoryValue(Geonet.Config.LANGUAGE_PROFILES_DIR);
		
		JeevesJCS.setConfigFilename(path + "WEB-INF/classes/cache.ccf");

		// force caches to be config'd so shutdown hook works correctly
		JeevesJCS.getInstance(Processor.XLINK_JCS);
		JeevesJCS.getInstance(XmlResolver.XMLRESOLVER_JCS);

		

		// --- Check current database and create database if an emty one is found
		String dbConfigurationFilePath = path + "/WEB-INF/config-db.xml";
		dbConfiguration = Xml.loadFile(dbConfigurationFilePath);
        ConfigurationOverrides.DEFAULT.updateWithOverrides(dbConfigurationFilePath, servletContext, path, dbConfiguration);

		Pair<Dbms,Boolean> pair = initDatabase(context);
		Dbms dbms = pair.one();
		Boolean created = pair.two();

		//------------------------------------------------------------------------
		//--- initialize thread pool 

		logger.info("  - Thread Pool...");

		threadPool = new ThreadPool();

		//------------------------------------------------------------------------
		//--- initialize settings subsystem

		logger.info("  - Setting manager...");
		SettingManager settingMan = null;
		HarvesterSettingsManager harvesterSettingsMan = null;
		try {
    		settingMan = new SettingManager(dbms, context.getProviderManager());
    		harvesterSettingsMan = new HarvesterSettingsManager(dbms, context.getProviderManager());
		} catch (Exception e) {
		    logger.info("     Failed to initialize setting managers. This is probably due to bad Settings table. Error is: " + 
		                e.getMessage() + ". In case of database migration, the setting managers will be reinitialized.");
		}
		
        // --- Migrate database if an old one is found
        migrateDatabase(servletContext, dbms, settingMan, harvesterSettingsMan, version, subVersion, context);
        
        if (settingMan == null) {
            throw new IllegalArgumentException("Failed to initialize setting managers. This is probably due to bad Settings table.");
        }
        
        // Reinitialized harvester manager which may be affected by migration
        if (harvesterSettingsMan == null) {
            harvesterSettingsMan = new HarvesterSettingsManager(dbms, context.getProviderManager());
        }
        
		//--- initialize ThreadUtils with setting manager and rm props
		ThreadUtils.init(context.getResourceManager().getProps(Geonet.Res.MAIN_DB),
		              	 settingMan); 


		//------------------------------------------------------------------------
		//--- initialize Z39.50

		logger.info("  - Z39.50...");

		boolean z3950Enable    = settingMan.getValueAsBool("system/z3950/enable", false);
		String  z3950port      = settingMan.getValue("system/z3950/port");

		// build Z3950 repositories file first from template
		URL url = getClass().getClassLoader().getResource(Geonet.File.JZKITCONFIG_TEMPLATE);

        if (Repositories.build(url, context)) {
			logger.info("     Repositories file built from template.");

			try {
			    JeevesApplicationContext appContext = context.getApplicationContext();

				// to have access to the GN context in spring-managed objects
				ContextContainer cc = (ContextContainer)appContext.getBean("ContextGateway");
				cc.setSrvctx(context);

				if (!z3950Enable)
					logger.info("     Server is Disabled.");
				else
				{
					logger.info("     Server is Enabled.");
		
					Server.init(z3950port, appContext);
				}	
			} catch (Exception e) {
				logger.error("     Repositories file init FAILED - Z3950 server disabled and Z3950 client services (remote search, harvesting) may not work. Error is:" + e.getMessage());
				e.printStackTrace();
			}
			
		} else {
			logger.error("     Repositories file builder FAILED - Z3950 server disabled and Z3950 client services (remote search, harvesting) may not work.");
		}

		//------------------------------------------------------------------------
		//--- initialize SchemaManager

		logger.info("  - Schema manager...");

		String schemaPluginsDir = handlerConfig.getMandatoryValue(Geonet.Config.SCHEMAPLUGINS_DIR);
		String schemaCatalogueFile = systemDataDir + "config" + File.separator + Geonet.File.SCHEMA_PLUGINS_CATALOG;
        boolean createOrUpdateSchemaCatalog = handlerConfig.getMandatoryValue(Geonet.Config.SCHEMA_PLUGINS_CATALOG_UPDATE).equals("true");
		logger.info("			- Schema plugins directory: "+schemaPluginsDir);
		logger.info("			- Schema Catalog File     : "+schemaCatalogueFile);
		SchemaManager schemaMan = SchemaManager.getInstance(path, Resources.locateResourcesDir(context), schemaCatalogueFile, schemaPluginsDir, context.getLanguage(), handlerConfig.getMandatoryValue(Geonet.Config.PREFERRED_SCHEMA), createOrUpdateSchemaCatalog);

		//------------------------------------------------------------------------
		//--- initialize search and editing

		logger.info("  - Search...");

		boolean logSpatialObject = "true".equalsIgnoreCase(handlerConfig.getMandatoryValue(Geonet.Config.STAT_LOG_SPATIAL_OBJECTS));
		boolean logAsynch = "true".equalsIgnoreCase(handlerConfig.getMandatoryValue(Geonet.Config.STAT_LOG_ASYNCH));
		logger.info("  - Log spatial object: " + logSpatialObject);
		logger.info("  - Log in asynch mode: " + logAsynch);
        
		String luceneTermsToExclude = "";
		luceneTermsToExclude = handlerConfig.getMandatoryValue(Geonet.Config.STAT_LUCENE_TERMS_EXCLUDE);

		LuceneConfig lc = new LuceneConfig(path, servletContext, luceneConfigXmlFile);
		logger.info("  - Lucene configuration is:");
		logger.info(lc.toString());
		beanFactory.registerSingleton(LuceneConfig.LUCENE_CONFIG_BEAN_NAME, lc);
       
		DataStore dataStore = context.getResourceManager().getDataStore(Geonet.Res.MAIN_DB);
		if (dataStore == null) {
			dataStore = createShapefileDatastore(luceneDir);
		}

		//--- no datastore for spatial indexing means that we can't continue
		if (dataStore == null) {
			throw new IllegalArgumentException("GeoTools datastore creation failed - check logs for more info/exceptions");
		}

		String maxWritesInTransactionStr = handlerConfig.getMandatoryValue(Geonet.Config.MAX_WRITES_IN_TRANSACTION);
		int maxWritesInTransaction = SpatialIndexWriter.MAX_WRITES_IN_TRANSACTION;
		try {
			maxWritesInTransaction = Integer.parseInt(maxWritesInTransactionStr);
		} catch (NumberFormatException nfe) {
			logger.error ("Invalid config parameter: maximum number of writes to spatial index in a transaction (maxWritesInTransaction), Using "+maxWritesInTransaction+" instead.");
			nfe.printStackTrace();
		}
	
		String htmlCacheDir = handlerConfig
				.getMandatoryValue(Geonet.Config.HTMLCACHE_DIR);
		
		SettingInfo settingInfo = new SettingInfo(settingMan);
		searchMan = new SearchManager(path, luceneDir, htmlCacheDir, thesauriDir, summaryConfigXmlFile, logAsynch,
				logSpatialObject, luceneTermsToExclude, dataStore, 
				maxWritesInTransaction, settingInfo, 
				schemaMan, servletContext, _applicationContext);
		
		 
		 // if the validator exists the proxyCallbackURL needs to have the external host and
		 // servlet name added so that the cas knows where to send the validation notice
		 ServerBeanPropertyUpdater.updateURL(settingInfo.getSiteUrl(true)+baseURL, servletContext);

		//------------------------------------------------------------------------
		//--- extract intranet ip/mask and initialize AccessManager

		logger.info("  - Access manager...");

		AccessManager accessMan = new AccessManager(dbms, settingMan);

		//------------------------------------------------------------------------
		//--- get edit params and initialize DataManager

		logger.info("  - Xml serializer and Data manager...");

		String useSubversion = handlerConfig.getMandatoryValue(Geonet.Config.USE_SUBVERSION);

		SvnManager svnManager = null;
		XmlSerializer xmlSerializer = null;
		if (useSubversion.equals("true")) {
			String subversionPath = handlerConfig.getValue(Geonet.Config.SUBVERSION_PATH);
			svnManager = new SvnManager(context, settingMan, subversionPath, dbms, created);
			xmlSerializer = new XmlSerializerSvn(settingMan, svnManager);
		} else {
			xmlSerializer = new XmlSerializerDb(settingMan);
		}
		
		DataManagerParameter dataManagerParameter = new DataManagerParameter();
		dataManagerParameter.context = context;
		dataManagerParameter.svnManager = svnManager;
		dataManagerParameter.searchManager = searchMan;
		dataManagerParameter.xmlSerializer = xmlSerializer;
		dataManagerParameter.schemaManager = schemaMan;
		dataManagerParameter.accessManager = accessMan;
		dataManagerParameter.dbms = dbms;
		dataManagerParameter.settingsManager = settingMan;
		dataManagerParameter.baseURL = baseURL;
		dataManagerParameter.dataDir = dataDir;
		dataManagerParameter.thesaurusDir = thesauriDir;
		dataManagerParameter.appPath = path;

		DataManager dataMan = new DataManager(dataManagerParameter);


        /**
         * Initialize iso languages mapper
         */
        IsoLanguagesMapper.getInstance().init(dbms);
        
        /**
         * Initialize language detector
         */
        LanguageDetector.init(path + languageProfilesDir, context, dataMan);

		//------------------------------------------------------------------------
		//--- Initialize thesaurus

		logger.info("  - Thesaurus...");

		ThesaurusManager thesaurusMan = ThesaurusManager.getInstance(context, path, dataMan, context.getResourceManager(), thesauriDir);

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Catalogue services for the web...");

		CatalogConfiguration.loadCatalogConfig(path, Csw.CONFIG_FILE);

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Open Archive Initiative (OAI-PMH) server...");

		OaiPmhDispatcher oaipmhDis = new OaiPmhDispatcher(settingMan, schemaMan);


        //------------------------------------------------------------------------
		//--- initialize metadata notifier subsystem
        MetadataNotifierManager metadataNotifierMan = new MetadataNotifierManager(dataMan);

        logger.info("  - Metadata notifier ...");

        
        //------------------------------------------------------------------------
        //--- initialize metadata notifier subsystem
        logger.info("  - Metadata notifier ...");

        //------------------------------------------------------------------------
        //--- initialize harvesting subsystem

        logger.info("  - Harvest manager...");

		GeonetContext gnContext = new GeonetContext();

        gnContext.springAppContext = context.getApplicationContext();
        gnContext.threadPool  = threadPool;
        gnContext.statusActionsClass = statusActionsClass;

        HarvestManager harvestMan = new HarvestManager(context, gnContext, harvesterSettingsMan, dataMan);

        //------------------------------------------------------------------------
        //--- return application context

		beanFactory.registerSingleton("accessManager", accessMan);
		beanFactory.registerSingleton("dataManager", dataMan);
		beanFactory.registerSingleton("searchManager", searchMan);
		beanFactory.registerSingleton("schemaManager", schemaMan);
		beanFactory.registerSingleton("serviceHandlerConfig", handlerConfig);
		beanFactory.registerSingleton("settingManager", settingMan);
		beanFactory.registerSingleton("harvesterSettingsMan", harvesterSettingsMan);
		beanFactory.registerSingleton("thesaurusManager", thesaurusMan);
		beanFactory.registerSingleton("oaipmhDisatcher", oaipmhDis);
		beanFactory.registerSingleton("metadataNotifierManager", metadataNotifierMan);
		beanFactory.registerSingleton("svnManager", svnManager);
		beanFactory.registerSingleton("xmlSerializer", xmlSerializer);
		beanFactory.registerSingleton("harvestManager", harvestMan);


        // Creates a default site logo, only if the logo image doesn't exists
        // This can happen if the application has been updated with a new version preserving the database and
        // images/logos folder is not copied from old application 
        createSiteLogo(gnContext.getSiteId(), servletContext, context.getAppPath());

        logger.info("Site ID is : " + gnContext.getSiteId());

        // Notify unregistered metadata at startup. Needed, for example, when the user enables the notifier config
        // to notify the existing metadata in database
        // TODO: Fix DataManager.getUnregisteredMetadata and uncomment next lines
        metadataNotifierControl = new MetadataNotifierControl(context, gnContext);
        metadataNotifierControl.runOnce();

		//--- load proxy information from settings into Jeeves for observers such
		//--- as jeeves.utils.XmlResolver to use
		ProxyInfo pi = JeevesProxyInfo.getInstance();
		boolean useProxy = settingMan.getValueAsBool("system/proxy/use", false);
		if (useProxy) {
			String  proxyHost      = settingMan.getValue("system/proxy/host");
			String  proxyPort      = settingMan.getValue("system/proxy/port");
			String  username       = settingMan.getValue("system/proxy/username");
			String  password       = settingMan.getValue("system/proxy/password");
			pi.setProxyInfo(proxyHost, Integer.valueOf(proxyPort), username, password);
		}

        //
        // db heartbeat configuration -- for failover to readonly database
        //
        boolean dbHeartBeatEnabled = Boolean.parseBoolean(handlerConfig.getValue(Geonet.Config.DB_HEARTBEAT_ENABLED, "false"));
        if(dbHeartBeatEnabled) {
            Integer dbHeartBeatInitialDelay = Integer.parseInt(handlerConfig.getValue(Geonet.Config.DB_HEARTBEAT_INITIALDELAYSECONDS, "5"));
            Integer dbHeartBeatFixedDelay = Integer.parseInt(handlerConfig.getValue(Geonet.Config.DB_HEARTBEAT_FIXEDDELAYSECONDS, "60"));
            createDBHeartBeat(context.getResourceManager(), gnContext, dbHeartBeatInitialDelay, dbHeartBeatFixedDelay);
        }
		return gnContext;
	}

    /**
     * Sets up a periodic check whether GeoNetwork can successfully write to the database. If it can't, GeoNetwork will
     * automatically switch to read-only mode.
     */
    private void createDBHeartBeat(final ResourceManager rm, final GeonetContext gc, Integer initialDelay, Integer fixedDelay) {
        logger.info("creating DB heartbeat with initial delay of " + initialDelay + " s and fixed delay of " + fixedDelay + " s" );
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable DBHeartBeat = new Runnable() {
            private static final String INSERT = "INSERT INTO Settings(id, parentId, name, value) VALUES(?, ?, ?, ?)";
            private static final String REMOVE = "DELETE FROM Settings WHERE id=?";

            /**
             *
             */
            @Override
            public void run() {
                try {
                    boolean readOnly = gc.isReadOnly();
                    logger.debug("DBHeartBeat: GN is read-only ? " + readOnly);
                    boolean canWrite = checkDBWrite();
                    HarvestManager hm = gc.getBean(HarvestManager.class);
                    if(readOnly && canWrite) {
                        logger.warning("GeoNetwork can write to the database, switching to read-write mode");
                        readOnly = false;
                        gc.setReadOnly(readOnly);
                        hm.setReadOnly(readOnly);
                    }
                    else if(!readOnly && !canWrite) {
                        logger.warning("GeoNetwork can not write to the database, switching to read-only mode");
                        readOnly = true;
                        gc.setReadOnly(readOnly);
                        hm.setReadOnly(readOnly);
                    }
                    else {
                        if(readOnly) {
                            logger.info("GeoNetwork remains in read-only mode");
                        }
                        else {
                            logger.debug("GeoNetwork remains in read-write mode");
                        }
                    }
                }
                // any uncaught exception would cause the scheduled execution to silently stop
                catch(Throwable x) {
                    logger.error("DBHeartBeat error: " + x.getMessage() + " This error is ignored.");
                    x.printStackTrace();
                }
            }

            /**
             *
             * @return
             */
            private boolean checkDBWrite() {
                Dbms dbms = null;
                try {
                    Integer testId = Integer.valueOf("100000");
                    dbms = (Dbms) rm.openDirect(Geonet.Res.MAIN_DB);
                    dbms.execute(INSERT, testId, Integer.valueOf("1"), "DBHeartBeat", "Yeah !");
                    dbms.execute(REMOVE, testId);
                    return true;
                }
                catch (Exception x) {
                    logger.info("DBHeartBeat Exception: " + x.getMessage());
                    return false;
                }
                finally {
                    try {
                        if (dbms != null) rm.close(Geonet.Res.MAIN_DB, dbms);
                    }
                    catch (Exception x) {
                        logger.error("DBHeartBeat failed to close DB connection. Your system is unstable! Error: " + x.getMessage());
                        x.printStackTrace();
                    }
                }
            }
        };
        scheduledExecutorService.scheduleWithFixedDelay(DBHeartBeat, initialDelay, fixedDelay, TimeUnit.SECONDS);
    }

    /**
     * Parses a version number removing extra "-*" element and returning an integer. "2.7.0-SNAPSHOT" is returned as 270.
     * 
     * @param number The version number to parse
     * @return The version number as an integer
     * @throws Exception
     */
    private int parseVersionNumber(String number) throws Exception {
        // Remove extra "-SNAPSHOT" info which may be in version number
        int dashIdx = number.indexOf("-");
        if (dashIdx != -1) {
            number = number.substring(0, number.indexOf("-"));
        }
        return Integer.valueOf(number.replaceAll("\\.", ""));
    }
    
    @SuppressWarnings("unchecked")
    /**
     * Return database version and subversion number.
     * 
     * @param dbms
     * @return
     */
    private Pair<String, String> getDatabaseVersion(Dbms dbms) {
        int VERSION_NUMBER_ID_BEFORE_2_11 = 15;
        int SUBVERSION_NUMBER_ID_BEFORE_2_11 = 16;
        String VERSION_NUMBER_KEY = "system/platform/version";
        String SUBVERSION_NUMBER_KEY = "system/platform/subVersion";
        String version = null;
        String subversion = null;
        
        List<Element> results;
        // Before 2.11, settings was a tree. Check using keys
        try {
            results = dbms.select("SELECT value FROM settings WHERE id = ?", VERSION_NUMBER_ID_BEFORE_2_11).getChildren();
            if (results.size() != 0) {
                Element record = (Element) results.get(0);
                version = record.getChildText("value");
            }
            results = dbms.select("SELECT value FROM settings WHERE id = ?", SUBVERSION_NUMBER_ID_BEFORE_2_11).getChildren();
            if (results.size() != 0) {
                Element record = (Element) results.get(0);
                subversion = record.getChildText("value");
            }
        } catch (SQLException e) {
            logger.info("     Error getting database version: " + e.getMessage() + 
                    ". Probably due to an old version. Trying with new Settings structure.");
            dbms.abort();
        }
        
        // Now settings is KVP
        if (version == null) {
            try {
                results = dbms.select("SELECT value FROM settings WHERE name = ?", VERSION_NUMBER_KEY).getChildren();
                if (results.size() != 0) {
                    Element record = (Element) results.get(0);
                    version = record.getChildText("value");
                }
                results = dbms.select("SELECT value FROM settings WHERE name = ?", SUBVERSION_NUMBER_KEY).getChildren();
                if (results.size() != 0) {
                    Element record = (Element) results.get(0);
                   subversion = record.getChildText("value");
                }
            } catch (SQLException e) {
                logger.info("     Error getting database version: " + e.getMessage() + ".");
            }
        }
        return Pair.read(version, subversion);
    }
	/**
	 * Checks if current database is running same version as the web application.
	 * If not, apply migration SQL script :
	 *  resources/sql/migration/{version}-to-{version}-{dbtype}.sql.
	 * eg. 2.4.3-to-2.5.0-default.sql
	 *
     * @param servletContext
	 * @param dbms
	 * @param settingMan
	 * @param harvesterSettingsMan TODO
	 * @param webappVersion
	 * @param subVersion
	 * @param appPath
     */
	private void migrateDatabase(ServletContext servletContext, Dbms dbms, SettingManager settingMan, 
	        HarvesterSettingsManager harvesterSettingsMan, String webappVersion, String subVersion, ServiceContext context) {
		logger.info("  - Migration ...");
		
		// Get db version and subversion
		Pair<String, String> dbVersionInfo = getDatabaseVersion(dbms);
		String dbVersion = dbVersionInfo.one();
		String dbSubVersion = dbVersionInfo.two();
		
		// Migrate db if needed
		logger.info("      Webapp   version:" + webappVersion + " subversion:" + subVersion);
		logger.info("      Database version:" + dbVersion + " subversion:" + dbSubVersion);
		if (dbVersion == null || webappVersion == null) {
			logger.warning("      Database does not contain any version information. Check that the database is a GeoNetwork database with data." + 
							"      Migration step aborted.");
			return;
		}
		
		int from = 0, to = 0;

		try {
		    from = parseVersionNumber(dbVersion);
		    to = parseVersionNumber(webappVersion);
		} catch(Exception e) {
		    logger.warning("      Error parsing version numbers: " + e.getMessage());
            e.printStackTrace();
		}
		
		if (from == to
				//&& subVersion.equals(dbSubVersion) Check only on version number
		) {
			logger.info("      Webapp version = Database version, no migration task to apply.");
		} else if (to > from) {
			boolean anyMigrationAction = false;
			boolean anyMigrationError = false;
			
			// Migrating from 2.0 to 2.5 could be done 2.0 -> 2.3 -> 2.4 -> 2.5
			String dbType = DatabaseType.lookup(dbms).toString();
			logger.debug("      Migrating from " + from + " to " + to + " (dbtype:" + dbType + ")...");
			
		    logger.info("      Loading SQL migration step configuration from config-db.xml ...");
            Element migrationConfig = dbConfiguration.getChild("migrate");
            if (migrationConfig != null) {
                @SuppressWarnings(value = "unchecked")
                List<Element> versions = migrationConfig.getChildren();
                for(Element version : versions) {
                    int versionNumber = Integer.valueOf(version.getAttributeValue("id"));
                    if (versionNumber > from && versionNumber <= to) {
                        logger.info("       - running tasks for " + versionNumber + "...");
                        @SuppressWarnings(value = "unchecked")
                        List<Element> versionConfiguration = version.getChildren();
                        for(Element file : versionConfiguration) {
                        	if(file.getName().equals("java")) {
    	                        try {
                                	String className = file.getAttributeValue("class");
                                    logger.info("         - Java migration class:" + className);
                                    
                                    // In 2.11, settingsManager was not able to initialized on previous
                                    // version db table due to structure changes
    	                        	if (settingMan != null && harvesterSettingsMan != null) {
                                        settingMan.refresh(dbms);
        	                            DatabaseMigrationTask task = (DatabaseMigrationTask) Class.forName(className).newInstance();
        	                            task.update(settingMan, harvesterSettingsMan, dbms);
    	                        	}
    	                        } catch (Exception e) {
    	                            logger.info("          Errors occurs during Java migration file: " + e.getMessage());
    	                            e.printStackTrace();
    	                            anyMigrationError = true;
    	                        }
                        	} else {
    	                        String filePath = path + file.getAttributeValue("path");
    	                        String filePrefix = file.getAttributeValue("filePrefix");
    	                        anyMigrationAction = true;
    	                        logger.info("         - SQL migration file:" + filePath + " prefix:" + filePrefix + " ...");
    	                        try {
    	                            Lib.db.insertData(servletContext, dbms, path, filePath, filePrefix);
    	                        } catch (Exception e) {
    	                            logger.info("          Errors occurs during SQL migration file: " + e.getMessage());
    	                            e.printStackTrace();
    	                            anyMigrationError = true;
    	                        }
                        	}
                        }
                    }
                }
            }
    		
			// Refresh setting manager in case the migration task added some new settings.
            try {
                if (settingMan != null) {
                    settingMan.refresh(dbms);
                } else {
                    // Reinitialized settings
                    settingMan = new SettingManager(dbms, context.getProviderManager());
                    
                    // Update the logo 
                    String siteId = settingMan.getValue("system/site/siteId");
                    initLogo(servletContext, dbms, siteId, context.getAppPath());
                }
            } catch (Exception e) {
                logger.info("      Errors occurs during settings manager refresh during migration. Error is: " + e.getMessage());
                e.printStackTrace();
                anyMigrationError = true;
            }
			
			// TODO : Maybe a force rebuild index is required in such situation.
			
			if (anyMigrationAction && !anyMigrationError) {
			    logger.info("      Successfull migration.\n" +
                        "      Catalogue administrator still need to update the catalogue\n" +
                        "      logo and data directory in order to complete the migration process.\n" +
                        "      Lucene index rebuild is also recommended after migration."
			            );
			}
			
			if (!anyMigrationAction) {
                logger.warning("      No migration task found between webapp and database version.\n" +
                        "      The system may be unstable or may failed to start if you try to run \n" +
                        "      the current GeoNetwork " + webappVersion + " with an older database (ie. " + dbVersion + "\n" +
                        "      ). Try to run the migration task manually on the current database\n" +
                        "      before starting the application or start with a new empty database.\n" +
                        "      Sample SQL scripts for migration could be found in WEB-INF/sql/migrate folder.\n"
                        );
                
            }
			
			if (anyMigrationError) {
                logger.warning("      Error occurs during migration. Check the log file for more details.");
            }
			// TODO : Maybe some migration stuff has to be done in Java ?
		} else {
	          logger.info("      Running on a newer database version.");
		}
	}

	/**
	 * Database initialization. If no table in current database
	 * create the GeoNetwork database. If an existing GeoNetwork database 
	 * exists, try to migrate the content.
	 * 
	 * @param context
	 * @return Pair with Dbms channel and Boolean set to true if db created
	 * @throws Exception
	 */
	private Pair<Dbms, Boolean> initDatabase(ServiceContext context) throws Exception {
		Dbms dbms = null;
		try {
			dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		} catch (Exception e) {
			logger.error("    Failed to open database connection, Check config.xml db file configuration.");
			logger.error(Util.getStackTrace(e));
			throw new IllegalArgumentException("No database connection");
		}
	
		String dbURL = dbms.getURL();
		logger.info("  - Database connection on " + dbURL + " ...");

        ServletContext servletContext = null;
        if(context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }

		Boolean created = false;
		// Create db if empty
		if (!Lib.db.touch(dbms)) {
			logger.info("      " + dbURL + " is an empty database (Metadata table not found).");

            @SuppressWarnings(value = "unchecked")
			List<Element> createConfiguration = dbConfiguration.getChild("create").getChildren();
			for(Element file : createConfiguration) {
			    String filePath = path + file.getAttributeValue("path");
			    String filePrefix = file.getAttributeValue("filePrefix");
			    logger.info("         - SQL create file:" + filePath + " prefix:" + filePrefix + " ...");
                // Do we need to remove object before creating the database ?
    			Lib.db.removeObjects(servletContext, dbms, path, filePath, filePrefix);
    			Lib.db.createSchema(servletContext, dbms, path, filePath, filePrefix);
			}
			
            @SuppressWarnings(value = "unchecked")
	        List<Element> dataConfiguration = dbConfiguration.getChild("data").getChildren();
	        for(Element file : dataConfiguration) {
                String filePath = path + file.getAttributeValue("path");
                String filePrefix = file.getAttributeValue("filePrefix");
                logger.info("         - SQL data file:" + filePath + " prefix:" + filePrefix + " ...");
                Lib.db.insertData(servletContext, dbms, path, filePath, filePrefix);
	        }
	        dbms.commit();
            
			// Copy logo
			String uuid = UUID.randomUUID().toString();
			initLogo(servletContext, dbms, uuid, context.getAppPath());
			
			context.getServlet().getEngine().loadConfigDB(dbms, -1);
			
			created = true;
		} else {
			logger.info("      Found an existing GeoNetwork database.");
		}

		return Pair.read(dbms, created);
	}

	/**
	 * Copy the default dummy logo to the logo folder based on uuid
     *
     * @param servletContext
     * @param dbms
* @param nodeUuid
* @param appPath
* @throws FileNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void initLogo(ServletContext servletContext, Dbms dbms, String nodeUuid, String appPath) {
		createSiteLogo(nodeUuid, servletContext, appPath);
		
		try {
			dbms.execute("UPDATE Settings SET value=? WHERE name='siteId'", nodeUuid);
		} catch (SQLException e) {
			logger.error("      Error when setting siteId values: " + e.getMessage());
		}
	}

    /**
     * Creates a default site logo, only if the logo image doesn't exists
     *
     * @param nodeUuid
     * @param servletContext
     * @param appPath
     */
    private void createSiteLogo(String nodeUuid, ServletContext servletContext, String appPath) {
        try {
            String logosDir = Resources.locateLogosDir(servletContext, appPath);
            File logo = new File(logosDir, nodeUuid +".gif");
            if (!logo.exists()) {
                FileOutputStream os = new FileOutputStream(logo);
                try {
                    os.write(Resources.loadImage(servletContext, appPath, "images/logos/dummy.gif", new byte[0]).one());
                    logger.info("      Setting catalogue logo for current node identified by: " + nodeUuid);
                } finally {
                    os.close();
                }
            }
        } catch (Exception e) {
            logger.error("      Error when setting the logo: " + e.getMessage());
        }
    }
	
	/**
	 * Set system properties to those required
	 * @param path webapp path
	 */
	private void setProps(String path, ServiceConfig handlerConfig) {

		String webapp = path + "WEB-INF" + FS;

		//--- Set jeeves.xml.catalog.files property
		//--- this is critical to schema support so must be set correctly
		String catalogProp = System.getProperty(Jeeves.XML_CATALOG_FILES);
		if (catalogProp == null) catalogProp = "";
		if (!catalogProp.equals("")) {
			logger.info("Overriding "+Jeeves.XML_CATALOG_FILES+" property (was set to "+catalogProp+")");
		} 
		catalogProp = webapp + "oasis-catalog.xml;" + handlerConfig.getValue(Geonet.Config.CONFIG_DIR) + File.separator + "schemaplugin-uri-catalog.xml";
		System.setProperty(Jeeves.XML_CATALOG_FILES, catalogProp);
		logger.info(Jeeves.XML_CATALOG_FILES+" property set to "+catalogProp);
		
		String blankXSLFile = path + "xsl" + FS + "blanks.xsl";
		System.setProperty(Jeeves.XML_CATALOG_BLANKXSLFILE, blankXSLFile);
		logger.info(Jeeves.XML_CATALOG_BLANKXSLFILE + " property set to " + blankXSLFile);
		
		//--- Set mime-mappings
		String mimeProp = System.getProperty("mime-mappings");
		if (mimeProp == null) mimeProp = "";
		if (!mimeProp.equals("")) {
			logger.info("Overriding mime-mappings property (was set to "+mimeProp+")");
		} 
		mimeProp = webapp + "mime-types.properties";
		System.setProperty("mime-mappings", mimeProp);
		logger.info("mime-mappings property set to "+mimeProp);

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

		try
		{
			searchMan.end();
		}
		catch (Exception e)
		{
			logger.error("Raised exception while stopping search");
			logger.error("  Exception : " +e);
			logger.error("  Message   : " +e.getMessage());
			logger.error("  Stack     : " +Util.getStackTrace(e));
		}

		
		logger.info("  - ThreadPool ...");
		threadPool.shutDown();
		
		logger.info("  - MetadataNotifier ...");
		try {
			metadataNotifierControl.shutDown();
		} catch (Exception e) {
			logger.error("Raised exception while stopping metadatanotifier");
			logger.error("  Exception : " +e);
			logger.error("  Message   : " +e.getMessage());
			logger.error("  Stack     : " +Util.getStackTrace(e));
		}

			
		logger.info("  - Harvest Manager...");
		_applicationContext.getBean(HarvestManager.class).shutdown();

		logger.info("  - Z39.50...");
		Server.end();
	}

	//---------------------------------------------------------------------------

	private DataStore createShapefileDatastore(String indexDir) throws Exception {

		File file = new File(indexDir + "/" + SPATIAL_INDEX_FILENAME + ".shp");
		if(!file.getParentFile().mkdirs() && !file.getParentFile().exists()) {
		    throw new RuntimeException("Unable to create the spatial index (shapefile) directory: "+file.getParentFile());
		}
		if (!file.exists()) {
			logger.info("Creating shapefile "+file.getAbsolutePath());
		} else {
			logger.info("Using shapefile "+file.getAbsolutePath());
		}
		IndexedShapefileDataStore ids = new IndexedShapefileDataStore(file.toURI().toURL(), new URI("http://geonetwork.org"), false, false, IndexType.QIX, Charset.forName(Jeeves.ENCODING));
		CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");

		if (crs != null) {
			ids.forceSchemaCRS(crs);
		}

		if (!file.exists()) {
			SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
			AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(DefaultGeographicCRS.WGS84).binding(MultiPolygon.class).buildDescriptor("the_geom");
			builder.setName(SPATIAL_INDEX_FILENAME);
			builder.add(geomDescriptor);
			builder.add(IDS_ATTRIBUTE_NAME, String.class);
			ids.createSchema(builder.buildFeatureType());
		}	

		logger.info("NOTE: Using shapefile for spatial index, this can be slow for larger catalogs");
		return ids;
	}
}

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jeeves.JeevesJCS;
import jeeves.JeevesProxyInfo;
import jeeves.interfaces.ApplicationHandler;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;
import jeeves.utils.ProxyInfo;
import jeeves.utils.XmlResolver;
import jeeves.xlink.Processor;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogDispatcher;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.lib.ServerLib;
import org.fao.geonet.notifier.MetadataNotifierControl;
import org.fao.geonet.notifier.MetadataNotifierManager;
import org.fao.geonet.services.util.z3950.Repositories;
import org.fao.geonet.services.util.z3950.Server;
import org.fao.geonet.util.ThreadPool;
import org.fao.geonet.util.ThreadUtils;
import org.geotools.data.DataStore;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.shapefile.indexed.IndexType;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.Element;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.vividsolutions.jts.geom.MultiPolygon;

//=============================================================================

/** This is the main class. It handles http connections and inits the system
  */

public class Geonetwork implements ApplicationHandler
{
	private Logger        		logger;
	private String 				path;				
	private SearchManager 		searchMan;
	private ThesaurusManager 	thesaurusMan;
	private MetadataNotifierControl metadataNotifierControl;
	private String						SPATIAL_INDEX_FILENAME    = "spatialindex";
	static final String				IDS_ATTRIBUTE_NAME        = "id";
	private ThreadPool        threadPool;
	private String   FS         = File.separator;

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

	/** Inits the engine, loading all needed data
	  */

	@SuppressWarnings("unchecked")
	public Object start(Element config, ServiceContext context) throws Exception
	{
		logger = context.getLogger();

		path    = context.getAppPath();
		String baseURL = context.getBaseUrl();
		String webappName = baseURL.substring(1);

		ServerLib sl = new ServerLib(path);
		String version = sl.getVersion();
		String subVersion = sl.getSubVersion();

		logger.info("Initializing GeoNetwork " + version +  "." + subVersion +  " ...");

		setProps(path);

		// Init directory path
		ServiceConfig handlerConfig = new ServiceConfig(config.getChildren());

		// Lucene
		String luceneSystemDir = System.getProperty(webappName + ".lucene.dir");
		String luceneDir = (luceneSystemDir != null ? luceneSystemDir : path
				+ handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_DIR));
		handlerConfig.setValue(Geonet.Config.LUCENE_DIR, luceneDir);
		System.setProperty(webappName + ".lucene.dir", luceneDir);
		
		logger.info("   - Lucene directory is:" + luceneDir);
		
		
		// Data directory
		String defaultDataDir = handlerConfig.getMandatoryValue(Geonet.Config.DATA_DIR);
		String dataSystemDir = System.getProperty(webappName + ".data.dir");
		if (dataSystemDir != null)
			initDataDirectory(dataSystemDir, path + defaultDataDir);
		
		String dataDir = (dataSystemDir != null ? dataSystemDir : path
				+ defaultDataDir);
		handlerConfig.setValue(Geonet.Config.DATA_DIR, dataDir);
		if (!new File(dataDir).isAbsolute())
			logger.info("   - Data directory is not an absolute path. Relative path is not recommended.\n" +
					"Update " + webappName + ".data.dir environment variable or dataDir parameter in config.xml." );
		
		
		String defaultThesaurusDir = handlerConfig.getValue(Geonet.Config.CODELIST_DIR, null);
                String thesaurusSystemDir = System.getProperty(webappName + ".codeList.dir");
                String thesauriDir = (thesaurusSystemDir != null ? thesaurusSystemDir : 
                                        (defaultThesaurusDir != null ? defaultThesaurusDir : dataDir + "/codelist/")
                                        );
		handlerConfig.setValue(Geonet.Config.CODELIST_DIR, thesauriDir);
		System.setProperty(webappName + ".codeList.dir", thesauriDir);
                
		System.setProperty(webappName + ".data.dir", dataDir);
                
		logger.info("   - Data directory is:" + dataDir);


		String luceneConfigXmlFile = handlerConfig
				.getMandatoryValue(Geonet.Config.LUCENE_CONFIG);
		String summaryConfigXmlFile = handlerConfig
				.getMandatoryValue(Geonet.Config.SUMMARY_CONFIG);
		String htmlCacheDir = handlerConfig
				.getMandatoryValue(Geonet.Config.HTMLCACHE_DIR);
		
		
		JeevesJCS.setConfigFilename(path+"WEB-INF/classes/cache.ccf");
		// force caches to be config'd so shutdown hook works correctly
		JeevesJCS jcsDummy = JeevesJCS.getInstance(Processor.XLINK_JCS);
		jcsDummy = JeevesJCS.getInstance(XmlResolver.XMLRESOLVER_JCS);

		

		// --- Check current database and create database if an emty one is found
		Dbms dbms = initDatabase(context);

		//------------------------------------------------------------------------
		//--- initialize thread pool 

		logger.info("  - Thread Pool...");

		threadPool = new ThreadPool();

		//------------------------------------------------------------------------
		//--- initialize settings subsystem

		logger.info("  - Setting manager...");

		SettingManager settingMan = new SettingManager(dbms, context.getProviderManager());

		// --- Migrate database if an old one is found
		migrateDatabase(dbms, settingMan, version, subVersion);
		
		//--- initialize ThreadUtils with setting manager and rm props
		ThreadUtils.init(context.getResourceManager().getProps(Geonet.Res.MAIN_DB),
		              	 settingMan); 

		//------------------------------------------------------------------------
		//--- Initialize thesaurus

		logger.info("  - Thesaurus...");

		thesaurusMan = ThesaurusManager.getInstance(path, thesauriDir);

		//------------------------------------------------------------------------
		//--- initialize Z39.50

		logger.info("  - Z39.50...");

		boolean z3950Enable    = settingMan.getValueAsBool("system/z3950/enable", false);
		String  z3950port      = settingMan.getValue("system/z3950/port");
		String  host           = settingMan.getValue("system/server/host");

		// null means not initialized
		ApplicationContext app_context = null;

		// build Z3950 repositories file first from template
		if (Repositories.build(path, context)) {
			logger.info("     Repositories file built from template.");

			try {
				app_context = new  ClassPathXmlApplicationContext( handlerConfig.getMandatoryValue( Geonet.Config.JZKITCONFIG )   );

				// to have access to the GN context in spring-managed objects
				ContextContainer cc = (ContextContainer)app_context.getBean("ContextGateway");
				cc.setSrvctx(context);

				if (!z3950Enable)
					logger.info("     Server is Disabled.");
				else
				{
					logger.info("     Server is Enabled.");
		
					UserSession session = new UserSession();
					session.authenticate(null, "z39.50", "", "", "Guest");
					context.setUserSession(session);
					context.setIpAddress("127.0.0.1");
					Server.init(host, z3950port, path, context, app_context);
				}	
			} catch (Exception e) {
				logger.error("     Repositories file init FAILED - Z3950 server disabled and Z3950 client services (remote search, harvesting) may not work. Error is:" 
						+ e.getMessage());
			}
			
		} else {
			logger.error("     Repositories file builder FAILED - Z3950 server disabled and Z3950 client services (remote search, harvesting) may not work.");
		}

		//------------------------------------------------------------------------
		//--- initialize SchemaManager

		logger.info("  - Schema manager...");

		String schemaPluginsDir = handlerConfig.getMandatoryValue(Geonet.Config.SCHEMAPLUGINS_DIR);
		SchemaManager schemaMan = SchemaManager.getInstance(path, schemaPluginsDir, context.getLanguage(), handlerConfig.getMandatoryValue(Geonet.Config.PREFERRED_SCHEMA));

		//------------------------------------------------------------------------
		//--- initialize search and editing

		logger.info("  - Search...");

		boolean logSpatialObject = "true".equalsIgnoreCase(handlerConfig.getMandatoryValue(Geonet.Config.STAT_LOG_SPATIAL_OBJECTS));
		boolean logAsynch = "true".equalsIgnoreCase(handlerConfig.getMandatoryValue(Geonet.Config.STAT_LOG_ASYNCH));
		logger.info("  - Log spatial object: " + logSpatialObject);
		logger.info("  - Log in asynch mode: " + logAsynch);
        
		String luceneTermsToExclude = "";
		luceneTermsToExclude = handlerConfig.getMandatoryValue(Geonet.Config.STAT_LUCENE_TERMS_EXCLUDE);
		LuceneConfig lc = new LuceneConfig(path, luceneConfigXmlFile);
        logger.info("  - Lucene configuration is:");
        logger.info(lc.toString());
        
		DataStore dataStore = createDataStore(context.getResourceManager().getProps(Geonet.Res.MAIN_DB), luceneDir);
		String maxWritesInTransactionStr = handlerConfig.getMandatoryValue(Geonet.Config.MAX_WRITES_IN_TRANSACTION);
		int maxWritesInTransaction = SpatialIndexWriter.MAX_WRITES_IN_TRANSACTION;
		try {
			maxWritesInTransaction = Integer.parseInt(maxWritesInTransactionStr);
		} catch (NumberFormatException nfe) {
			logger.error ("Invalid config parameter: maximum number of writes to spatial index in a transaction (maxWritesInTransaction), Using "+maxWritesInTransaction+" instead.");
			nfe.printStackTrace();
		}
	
		searchMan = new SearchManager(path, luceneDir, htmlCacheDir, dataDir, summaryConfigXmlFile, lc, 
				logAsynch, logSpatialObject, luceneTermsToExclude, 
				dataStore, maxWritesInTransaction, 
				new SettingInfo(settingMan), schemaMan);

		//------------------------------------------------------------------------
		//--- extract intranet ip/mask and initialize AccessManager

		logger.info("  - Access manager...");

		AccessManager accessMan = new AccessManager(dbms, settingMan);

		//------------------------------------------------------------------------
		//--- get edit params and initialize DataManager

		logger.info("  - Data manager...");

		File _htmlCacheDir = new File(htmlCacheDir);
		if (!_htmlCacheDir.isAbsolute()) {
			htmlCacheDir = path + htmlCacheDir;
		}
		DataManager dataMan = new DataManager(context, schemaMan, searchMan, accessMan, dbms, settingMan, baseURL, htmlCacheDir, dataDir, path);

		//------------------------------------------------------------------------
		//--- initialize harvesting subsystem

		logger.info("  - Harvest manager...");

		HarvestManager harvestMan = new HarvestManager(context, settingMan, dataMan);
		dataMan.setHarvestManager(harvestMan);

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Catalogue services for the web...");

		CatalogConfiguration.loadCatalogConfig(path, Csw.CONFIG_FILE);
		CatalogDispatcher catalogDis = new CatalogDispatcher(new File(path,summaryConfigXmlFile), lc);

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Open Archive Initiative (OAI-PMH) server...");

		OaiPmhDispatcher oaipmhDis = new OaiPmhDispatcher(settingMan, schemaMan);


        //------------------------------------------------------------------------
		//--- initialize metadata notifier subsystem
        MetadataNotifierManager metadataNotifierMan = new MetadataNotifierManager(dataMan);

        logger.info("  - Metadata notifier ...");

		//------------------------------------------------------------------------
		//--- return application context

		GeonetContext gnContext = new GeonetContext();

		gnContext.accessMan   = accessMan;
		gnContext.dataMan     = dataMan;
		gnContext.searchMan   = searchMan;
		gnContext.schemaMan   = schemaMan;
		gnContext.config      = handlerConfig;
		gnContext.catalogDis  = catalogDis;
		gnContext.settingMan  = settingMan;
		gnContext.harvestMan  = harvestMan;
		gnContext.thesaurusMan= thesaurusMan;
		gnContext.oaipmhDis   = oaipmhDis;
		gnContext.app_context = app_context;
    gnContext.metadataNotifierMan = metadataNotifierMan;
		gnContext.threadPool  = threadPool;

		logger.info("Site ID is : " + gnContext.getSiteId());

        // Creates a default site logo, only if the logo image doesn't exists
        // This can happen if the application has been updated with a new version preserving the database and
        // images/logos folder is not copied from old application 
        createSiteLogo(gnContext.getSiteId());

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
			pi.setProxyInfo(proxyHost, new Integer(proxyPort), username, password);
		}
	

		return gnContext;
	}


	/**
	 * Check if data directory is empty or not. If empty,
	 * add mandatory elements (codelist).
	 *  
	 * @param dataSystemDir
	 * @param defaultDataDir 
	 */
	private void initDataDirectory(String dataSystemDir, String defaultDataDir) {
		logger.info("   - Data directory initialization ...");
		File dataDir = new File(dataSystemDir);
		if (!dataDir.exists()) {
			dataDir.mkdir();
		}
		File codelistDir = new File(dataSystemDir + "/codelist");
		if (!codelistDir.exists()) {
			logger.info("     - Copying codelists directory ...");
			try {
				BinaryFile.copyDirectory(new File(defaultDataDir + "/codelist"), codelistDir);
			} catch (IOException e) {			
				logger.info("     - Copy failed: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check if current database is running same version as the web application.
	 * If not, apply migration SQL script :
	 *  resources/sql/migration/{version}-to-{version}-{dbtype}.sql.
	 * eg. 2.4.3-to-2.5.0-default.sql
	 * 
	 * @param dbms
	 * @param settingMan
	 * @param version
	 * @param subVersion
	 */
	private void migrateDatabase(Dbms dbms, SettingManager settingMan, String version, String subVersion) {
		logger.info("  - Migration ...");
		
		// Get db version and subversion
		String dbVersion = settingMan.getValue("system/platform/version");
		String dbSubVersion = settingMan.getValue("system/platform/subVersion");
		
		// Migrate db if needed
		logger.debug("      Webapp   version:" + version + " subversion:" + subVersion);
		logger.debug("      Database version:" + dbVersion + " subversion:" + dbSubVersion);
		
		if (version.equals(dbVersion)
				//&& subVersion.equals(dbSubVersion) Check only on version number
		) {
			logger.info("      Webapp version = Database version, no migration task to apply.");
		} else {
			// Migrating from 2.0 to 2.5 could be done 2.0 -> 2.3 -> 2.4 -> 2.5
			String dbType = Lib.db.getDBType(dbms);
			logger.info("      Migrating from " + dbVersion + " to " + version + " (dbtype:" + dbType + ")...");
			String sqlScriptPath = "";
			String sqlMigrationScriptPath = path + "/WEB-INF/classes/setup/sql/migrate/" + 
				 dbVersion + "-to-" + version + "/" + dbType + ".sql";
			String defaultSqlMigrationScriptPath = path + "/WEB-INF/classes/setup/sql/migrate/" + 
			 dbVersion + "-to-" + version + "/default.sql";
		
			File sqlMigrationScript = new File(sqlMigrationScriptPath);
			File defaultSqlMigrationScript = new File(defaultSqlMigrationScriptPath);
			File script = null;
			
			// Use specific db script or default one
			if (sqlMigrationScript.exists()) {
				script = sqlMigrationScript;
			} else if (defaultSqlMigrationScript.exists()) {
				script = defaultSqlMigrationScript;
			} 
			
			if (script != null) {
				try {
					sqlScriptPath = script.getCanonicalPath();
					// Run the SQL migration
					logger.info("      Running SQL migration step ...");
					Lib.db.runSQL(dbms, script);
					
					// Refresh setting manager in case the migration task added some new settings.
					settingMan.refresh(dbms);
					
					// Update the logo 
					String siteId = settingMan.getValue("system/site/siteId");
					initLogo(dbms, siteId);
					
					// TODO : Maybe a force rebuild index is required in such situation.
				} catch (Exception e) {
					logger.info("      Errors occurs during SQL migration task: " + sqlScriptPath 
							+ " or when refreshing settings manager.");
					e.printStackTrace();
				}
				
				logger.info("      Successfull migration.\n" +
							"      Catalogue administrator still need to update the catalogue\n" +
							"      logo and data directory in order to complete the migration process.\n" +
							"      Lucene index rebuild is also recommended after migration."
				);
				
			} else {
				logger.info("      No migration task found between webapp and database version.\n" +
						"      The system may be unstable or may failed to start if you try to run \n" +
						"      the current GeoNetwork " + version + " with an older database (ie. " + dbVersion + "\n" +
						"      ). Try to run the migration task manually on the current database\n" +
						"      before starting the application or start with a new empty database.\n" +
						"      Sample SQL scripts for migration could be found in WEB-INF/sql/migrate folder.\n"
						);
				
			}
			
			// TODO : Maybe some migration stuff has to be done in Java ?
		}
	}

	/**
	 * Database initialization. If no table in current database
	 * create the GeoNetwork database. If an existing GeoNetwork database 
	 * exists, try to migrate the content.
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	private Dbms initDatabase(ServiceContext context) throws Exception {
		Dbms dbms = null;
		try {
			dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		} catch (Exception e) {
			logger.info("    Failed to open database connection, Check config.xml db file configuration."
					+ "Error is: " + e.getMessage());
			e.printStackTrace();
		}
		
		String dbURL = dbms.getURL();
		logger.info("  - Database connection on " + dbURL + " ...");
		
		// Create db if empty
		if (!Lib.db.touch(dbms)) {
			logger.info("      " + dbURL + " is an empty database (Metadata table not found).");
			
			// Do we need to remove object before creating the database ?
			Lib.db.removeObjects(dbms, path);
			Lib.db.createSchema(dbms, path);
			dbms.commit();
			Lib.db.insertData(dbms, path);
			
			// Copy logo
			String uuid = UUID.randomUUID().toString();
			initLogo(dbms, uuid);
	
		} else {
			logger.info("      Found an existing GeoNetwork database.");
		}
		
		return dbms;
	}

	/**
	 * Copy the default dummy logo to the logo folder based on uuid
	 * @param dbms
	 * @param nodeUuid
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void initLogo(Dbms dbms, String nodeUuid) {
		createSiteLogo(nodeUuid);
		
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
     */
    private void createSiteLogo(String nodeUuid) {
        try {
            File logo = new File(path +"/images/logos/"+ nodeUuid +".gif");
            if (!logo.exists()) {
                FileInputStream  is = new FileInputStream (path +"/images/logos/dummy.gif");
                FileOutputStream os = new FileOutputStream(path +"/images/logos/"+ nodeUuid +".gif");
                logger.info("      Setting catalogue logo for current node identified by: " + nodeUuid);
                BinaryFile.copy(is, os, true, true);
            }
        } catch (Exception e) {
            logger.error("      Error when setting the logo: " + e.getMessage());
        }
    }
	
	/**
	 * Set system properties to those required
	 * @param path webapp path
	 */
	private void setProps(String path) {

		String webapp = path + "WEB-INF" + FS;

		//--- Set xml.catalog.files property
		//--- this is critical to schema support so must be set correctly
		String catalogProp = System.getProperty("xml.catalog.files");
		if (catalogProp == null) catalogProp = "";
		if (!catalogProp.equals("")) {
			logger.info("Overriding xml.catalog.files property (was set to "+catalogProp+")");
		} 
		catalogProp = webapp + "oasis-catalog.xml" + ";" + webapp + "schemaplugin-uri-catalog.xml";
		System.setProperty("xml.catalog.files", catalogProp);
		logger.info("xml.catalog.files property set to "+catalogProp);

		//--- Set mime-mappings
		String mimeProp = System.getProperty("mime-mappings");
		if (mimeProp == null) mimeProp = "";
		if (!mimeProp.equals("")) {
			logger.info("Overriding mime-mappings property (was set to "+mimeProp+")");
		} 
		mimeProp = webapp + "mime-types.properties";
		System.setProperty("mime-mappings", mimeProp);
		logger.info("mime-mappings property set to "+mimeProp);

		return;
	}
		
		
	//---------------------------------------------------------------------------
	//---
	//--- Stop
	//---
	//---------------------------------------------------------------------------

	public void stop()
	{
		logger.info("Stopping geonetwork...");

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

		//------------------------------------------------------------------------
		//--- end Z39.50
		logger.info("  - Z39.50...");
		
		
		logger.info("  - ThreadPool ...");
		threadPool.shutDown();
		
		logger.info("  - MetadataNotifier ...");
		metadataNotifierControl.shutDown();

		Server.end();
	}

	
	
	//---------------------------------------------------------------------------

	private DataStore createDataStore(Map<String,String> props, String luceneDir) throws Exception {
		String url = props.get("url");
		String user = props.get("user");
		String passwd = props.get("password");

		DataStore ds = null;
		try {
			if (url.contains("postGIS")) {
				ds = createPostgisDatastore(user, passwd, url);
			} else if (url.contains("oracle")) {
				ds = createOracleDatastore(user, passwd, url);
			}
		} catch (Exception e) {
			logger.error("Failed to create datastore for "+url+". Will use shapefile instead.");
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		if (ds != null) return ds;
		else return createShapefileDatastore(luceneDir);
	}

	//---------------------------------------------------------------------------

	private DataStore createPostgisDatastore(String user, String passwd, String url) throws Exception {

		String[] values = url.split("/");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(PostgisDataStoreFactory.DBTYPE.key, PostgisDataStoreFactory.DBTYPE.sample);
		params.put(PostgisDataStoreFactory.DATABASE.key, getDatabase(url, values));
		params.put(PostgisDataStoreFactory.USER.key, user);
		params.put(PostgisDataStoreFactory.PASSWD.key, passwd);
		params.put(PostgisDataStoreFactory.HOST.key, getHost(url, values));
		params.put(PostgisDataStoreFactory.PORT.key, getPort(url, values));
		//logger.info("Connecting using "+params); - don't show unless we need it

		PostgisDataStoreFactory factory = new PostgisDataStoreFactory();
		DataStore ds = factory.createDataStore(params);
		logger.info("NOTE: Using POSTGIS for spatial index");

		return ds;
	}

	//---------------------------------------------------------------------------

	private DataStore createOracleDatastore(String user, String passwd, String url) throws Exception {

/*
		String[] values = url.split(":");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(OracleDataStoreFactory.DBTYPE.key, OracleDataStoreFactory.DBTYPE.sample);
		params.put(OracleDataStoreFactory.DATABASE.key, getDatabase(url, values));
		params.put(OracleDataStoreFactory.USER.key, user);
		params.put(OracleDataStoreFactory.PASSWD.key, passwd);
		params.put(OracleDataStoreFactory.HOST.key, getHost(url, values));
		params.put(OracleDataStoreFactory.PORT.key, getPort(url, values));

		OracleDataStoreFactory factory = new OracleDataStoreFactory();
		DataStore ds = factory.createDataStore(params);

		return ds;
*/
		return null;
	}

	//---------------------------------------------------------------------------

	private DataStore createShapefileDatastore(String luceneDir) throws Exception {
		File file = new File(luceneDir + "/spatial/" + SPATIAL_INDEX_FILENAME + ".shp");
		file.getParentFile().mkdirs();
		if (!file.exists()) {
			logger.info("Creating shapefile "+file.getAbsolutePath());
		} else {
			logger.info("Using shapefile "+file.getAbsolutePath());
		}
		IndexedShapefileDataStore ds = new IndexedShapefileDataStore(file.toURI().toURL(), new URI("http://geonetwork.org"), true, true, IndexType.QIX, Charset.defaultCharset());
		CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");

		if (crs != null) {
			ds.forceSchemaCRS(crs);
		}

		if (!file.exists()) {
			SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
			AttributeDescriptor geomDescriptor = new AttributeTypeBuilder().crs(DefaultGeographicCRS.WGS84).binding(MultiPolygon.class).buildDescriptor("the_geom");
			builder.setName(SPATIAL_INDEX_FILENAME);
			builder.add(geomDescriptor);
			builder.add(IDS_ATTRIBUTE_NAME, String.class);
			ds.createSchema(builder.buildFeatureType());
		}	

		logger.info("NOTE: Using shapefile for spatial index, this can be slow for larger catalogs");
		return ds;
	}

	//---------------------------------------------------------------------------

	private String getDatabase(String url, String[] values) throws Exception {
		if (url.contains("postGIS")) {
			return values[3];
		} else if (url.contains("oracle")) {
			return values[5];
		} else {
			throw new Exception("Unknown database in url "+url);
		}
	}

	//---------------------------------------------------------------------------

	private String getHost(String url, String[] values) throws Exception {
		if (url.contains("postGIS")) {
			String value = values[2];
			return value.substring(0,value.indexOf(':'));
		} else if (url.contains("oracle")) {
			return values[3];
		} else {
			throw new Exception("Unknown database in url "+url);
		}
	}

	//---------------------------------------------------------------------------

	private String getPort(String url, String values[]) throws Exception {
		if (url.contains("postGIS")) {
			String value = values[2];
			return value.substring(value.indexOf(':')+1);
		} else if (url.contains("oracle")) {
			return values[4];
		} else {
			throw new Exception("Unknown database in url "+url);
		}
	}
}

//=============================================================================


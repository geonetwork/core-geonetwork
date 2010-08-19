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
import jeeves.JeevesJCS;
import jeeves.interfaces.ApplicationHandler;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;
import jeeves.xlink.Processor;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogDispatcher;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.lib.ServerLib;
import org.fao.geonet.services.util.z3950.Repositories;
import org.fao.geonet.services.util.z3950.Server;
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

//=============================================================================

/** This is the main class. It handles http connections and inits the system
  */

public class Geonetwork implements ApplicationHandler
{
	private Logger        		logger;
	private String 				path;				
	private SearchManager 		searchMan;
	private ThesaurusManager 	thesaurusMan;
	private String						SPATIAL_INDEX_FILENAME    = "spatialindex";
	static final String				IDS_ATTRIBUTE_NAME        = "id";

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

	public Object start(Element config, ServiceContext context) throws Exception
	{
		logger = context.getLogger();

		path    = context.getAppPath();
		String baseURL = context.getBaseUrl();

		ServerLib sl = new ServerLib(path);
		String version = sl.getVersion();
		String subVersion = sl.getSubVersion();
		
		logger.info("Initializing GeoNetwork " + version +  "." + subVersion +  " ...");

		JeevesJCS.setConfigFilename(path+"WEB-INF/classes/cache.ccf");
		// force cache to be config'd so shutdown hook works correctly
		JeevesJCS jcsDummy = JeevesJCS.getInstance(Processor.XLINK_JCS);

		ServiceConfig handlerConfig = new ServiceConfig(config.getChildren());

		// --- Check current database and create database if an emty one is found
		Dbms dbms = initDatabase(context);

		//------------------------------------------------------------------------
		//--- initialize settings subsystem

		logger.info("  - Setting manager...");

		SettingManager settingMan = new SettingManager(dbms, context.getProviderManager());

		
		// --- Migrate database if an old one is found
		migrateDatabase(dbms, settingMan, version, subVersion);
		
		//------------------------------------------------------------------------
		//--- Initialize thesaurus

		logger.info("  - Thesaurus...");

		String thesauriDir = handlerConfig.getMandatoryValue(Geonet.Config.CODELIST_DIR);

		thesaurusMan = new ThesaurusManager(path, thesauriDir);


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
		} else {
			logger.error("     Repositories file builder FAILED - Z3950 server disabled and Z3950 client services (remote search, harvesting) may not work.");
		}

		//------------------------------------------------------------------------
		//--- initialize search and editing

		logger.info("  - Search...");

		String htmlCacheDir = handlerConfig.getMandatoryValue(Geonet.Config.HTMLCACHE_DIR);
		String luceneDir = path + handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_DIR);
		String summaryConfigXmlFile = handlerConfig.getMandatoryValue(Geonet.Config.SUMMARY_CONFIG);
		String dataDir = path + handlerConfig.getMandatoryValue(Geonet.Config.DATA_DIR);
        String guiConfigXmlFile = handlerConfig.getMandatoryValue(Geonet.Config.GUI_CONFIG);
        String luceneConfigXmlFile = handlerConfig.getMandatoryValue(Geonet.Config.LUCENE_CONFIG);
        
		DataStore dataStore = createDataStore(context.getResourceManager().getProps(Geonet.Res.MAIN_DB), luceneDir);
	
		searchMan = new SearchManager(path, luceneDir, htmlCacheDir, summaryConfigXmlFile, guiConfigXmlFile, luceneConfigXmlFile, dataStore, new SettingInfo(settingMan));

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
		DataManager dataMan = new DataManager(context, searchMan, accessMan, dbms, settingMan, baseURL, htmlCacheDir, dataDir, path);

		String schemasDir = path + Geonet.Path.SCHEMAS;
		String saSchemas[] = new File(schemasDir).list();

		if (saSchemas == null)
			throw new Exception("Cannot scan schemas directory : " +schemasDir);
		else
		{
			for(int i=0; i<saSchemas.length; i++)
				if (!saSchemas[i].equals("CVS") && !saSchemas[i].startsWith("."))
				{
					logger.info("    Adding xml schema : " +saSchemas[i]);
					String schemaFile  = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA;
					String suggestFile = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA_SUGGESTIONS;
					String substitutesFile = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA_SUBSTITUTES;

					dataMan.addSchema(saSchemas[i], schemaFile, suggestFile, substitutesFile);
				}
		}

		//------------------------------------------------------------------------
		//--- initialize harvesting subsystem

		logger.info("  - Harvest manager...");

		HarvestManager harvestMan = new HarvestManager(context, settingMan, dataMan);
		dataMan.setHarvestManager(harvestMan);

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		logger.info("  - Catalogue services for the web...");

		CatalogConfiguration.loadCatalogConfig(path, Csw.CONFIG_FILE);
		CatalogDispatcher catalogDis = new CatalogDispatcher(new File(path,summaryConfigXmlFile), new File(path,luceneConfigXmlFile));

		//------------------------------------------------------------------------
		//--- initialize catalogue services for the web

		int oaimode    = settingMan.getValueAsInt("system/oai/mdmode");
		int  oaicachesize      = settingMan.getValueAsInt("system/oai/cachesize");
		int  oaicachelifetime  = settingMan.getValueAsInt("system/oai/tokentimeout");
		logger.info("  - Open Archive Initiative (OAI-PMH) server: mode"+oaimode+" cachesize: "+oaicachesize+" cachelifetime: "+oaicachelifetime);

		OaiPmhDispatcher oaipmhDis = new OaiPmhDispatcher(oaimode,oaicachesize,oaicachelifetime);

		//------------------------------------------------------------------------
		//--- return application context

		GeonetContext gnContext = new GeonetContext();

		gnContext.accessMan   = accessMan;
		gnContext.dataMan     = dataMan;
		gnContext.searchMan   = searchMan;
		gnContext.config      = handlerConfig;
		gnContext.catalogDis  = catalogDis;
		gnContext.settingMan  = settingMan;
		gnContext.harvestMan  = harvestMan;
		gnContext.thesaurusMan= thesaurusMan;
		gnContext.oaipmhDis   = oaipmhDis;
		gnContext.app_context = app_context;

		logger.info("Site ID is : " + gnContext.getSiteId());

		return gnContext;
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
			String sqlMigrationScriptPath = path + "/WEB-INF/classes/setup/sql/migrate/" + 
				 dbVersion + "-to-" + version + "/" + dbType + ".sql";
			File sqlMigrationScript = new File(sqlMigrationScriptPath);
			if (sqlMigrationScript.exists()) {
				try {
					// Run the SQL migration
					logger.info("      Running SQL migration step ...");
					Lib.db.runSQL(dbms, sqlMigrationScript);
					
					// Refresh setting manager in case the migration task added some new settings.
					settingMan.refresh();
					
					// Update the logo 
					String siteId = settingMan.getValue("system/site/siteId");
					initLogo(dbms, siteId);
					
					// TODO : Maybe a force rebuild index is required in such situation.
				} catch (Exception e) {
					logger.info("      Errors occurs during SQL migration task: " + sqlMigrationScriptPath 
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
	 * @param appPath
	 * @param nodeUuid
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void initLogo(Dbms dbms, String nodeUuid) {
		try {
			FileInputStream  is = new FileInputStream (path +"/images/logos/dummy.gif");
			FileOutputStream os = new FileOutputStream(path +"/images/logos/"+ nodeUuid +".gif");
			logger.info("      Setting catalogue logo for current node identified by: " + nodeUuid);
			BinaryFile.copy(is, os, true, true);
		} catch (Exception e) {
			logger.error("      Error when setting the logo: " + e.getMessage());
		}
		
		try {
			dbms.execute("UPDATE Settings SET value=? WHERE name='siteId'", nodeUuid);
		} catch (SQLException e) {
			logger.error("      Error when setting siteId values: " + e.getMessage());
		}
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
		Server.end();
	}

	
	
	//---------------------------------------------------------------------------

	private DataStore createDataStore(Map<String,String> props, String luceneDir) throws Exception {
		String url = props.get("url");
		String user = props.get("user");
		String passwd = props.get("password");

		DataStore ds = null;
		try {
			if (url.contains("postgis")) {
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

		String[] values = url.split(":");
/*
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
		if (url.contains("postgis")) {
			return values[3];
		} else if (url.contains("oracle")) {
			return values[5];
		} else {
			throw new Exception("Unknown database in url "+url);
		}
	}

	//---------------------------------------------------------------------------

	private String getHost(String url, String[] values) throws Exception {
		if (url.contains("postgis")) {
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
		if (url.contains("postgis")) {
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

